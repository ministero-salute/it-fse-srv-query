package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.composer.HistoryComposer;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.composer.SimpleComposer;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types.CompactCS;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types.CompactVS;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistorySnapshotDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.Optional;

import static ca.uhn.fhir.model.api.TemporalPrecisionEnum.MILLI;
import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static ca.uhn.fhir.rest.api.SummaryEnum.TRUE;
import static java.time.ZoneOffset.UTC;
import static java.util.TimeZone.getTimeZone;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.hl7.fhir.r4.model.SearchParameter.SP_STATUS;

public abstract class HistoryAbstractClient {

    private static final int CHUNK_SIZE = 10;

    protected final IGenericClient client;

    public HistoryAbstractClient(IGenericClient client) {
        this.client = client;
    }

    protected HistorySnapshotDTO createSnapshot() {
        // Create timestamp
        Date timestamp = new Date();
        // Create composer
        SimpleComposer composer = new SimpleComposer(
            client,
            getCurrentActiveResources(CodeSystem.class),
            getCurrentActiveResources(ValueSet.class)
        );
        // Convert
        return HistorySnapshotDTO.from(timestamp, composer.compose());
    }

    protected RawHistoryDTO createHistoryByLastUpdate(Date lastUpdate) {
        RawHistoryDTO out;
        if(lastUpdate == null) {
            out = createHistoryFromBegins();
        } else {
            out = createHistoryFromDate(lastUpdate);
        }
        return out;
    }

    private RawHistoryDTO createHistoryFromBegins() {
        // Create timestamp
        Date timestamp = new Date();
        // Create composer
        SimpleComposer composer = new SimpleComposer(
            client,
            getCurrentActiveResources(CodeSystem.class),
            getCurrentActiveResources(ValueSet.class)
        );
        // Retrieve resources
        return new RawHistoryDTO(
            timestamp,
            null,
            composer.compose()
        );
    }

    private RawHistoryDTO createHistoryFromDate(Date lastUpdate) {
        // Create timestamp to sync both queries
        Date timestamp = new Date();
        // Create composer
        HistoryComposer composer = new HistoryComposer(
            client,
            getHistoryFromDateButUntil(CodeSystem.class, lastUpdate, timestamp),
            getHistoryFromDateButUntil(ValueSet.class, lastUpdate, timestamp)
        );
        // Retrieve resources
        return new RawHistoryDTO(
            timestamp,
            lastUpdate,
            composer.compose()
        );
    }

    private Bundle getCurrentActiveResources(Class<? extends BaseResource> type) {
        return client
            .search()
            .forResource(type)
            .where(isActiveResource())
            .cacheControl(noCache())
            .returnBundle(Bundle.class)
            .count(CHUNK_SIZE)
            .summaryMode(TRUE)
            .execute();
    }

    private Bundle getHistoryFromDateButUntil(Class<? extends BaseResource> type, Date lastUpdate, Date until) {
        return client
            .history()
            .onType(type)
            .returnBundle(Bundle.class)
            .cacheControl(noCache())
            .at(new DateRangeParam(getTimeUTC(lastUpdate), getTimeUTC(until)))
            .summaryMode(TRUE)
            .count(CHUNK_SIZE)
            .execute();
    }

    protected Optional<HistoryResourceResDTO> getMappedResource(String resourceId, String versionId) throws MalformedResourceException {
        // Working var
        HistoryResourceResDTO out = null;
        // Get resource
        Optional<IBaseResource> wrapper = getResource(resourceId, versionId);
        // Check if found
        if(wrapper.isPresent()) {
            // Retrieve resource
            out = mapResource(wrapper.get(), resourceId, versionId);
        }
        return Optional.ofNullable(out);
    }

    private HistoryResourceResDTO mapResource(IBaseResource resource, String resourceId, String versionId) throws MalformedResourceException {
        // Working var
        HistoryResourceResDTO res;
        // Check type
        ResourceType type = ResourceType.fromCode(resource.fhirType());
        // Start mapping
        if(type == ResourceType.CodeSystem) {
            res = new CompactCS(resourceId, versionId, (CodeSystem) resource).convert();
        } else if(type == ResourceType.ValueSet) {
            res = new CompactVS(resourceId, versionId, (ValueSet) resource).convert();
        }else {
            throw new IllegalArgumentException("Unknown type to map: " + type);
        }
        return res;
    }

    private Optional<IBaseResource> getResource(String resourceId, String versionId) {
        IBaseResource out = getResourceByType(CodeSystem.class, resourceId, versionId);
        if(out == null) {
            out = getResourceByType(ValueSet.class, resourceId, versionId);
            if(out != null) {
                isExpansionNeeded((ValueSet) out, resourceId, versionId);
            }
        }
        return Optional.ofNullable(out);
    }

    private void isExpansionNeeded(ValueSet vs, String resourceId, String versionId) {
        // Expand only if empty
        if(vs.getExpansion().isEmpty()) {
            applyResourceExpansion(vs, resourceId, versionId);
        }
    }

    private void applyResourceExpansion(ValueSet vs, String resourceId, String versionId) {
        ValueSet expanded = client
            .operation()
            .onInstance(new IdType(ResourceType.ValueSet.name(), resourceId, versionId))
            .named("expand")
            .withNoParameters(Parameters.class)
            .cacheControl(noCache())
            .returnResourceType(ValueSet.class)
            .execute();
        // Assign
        vs.setExpansion(expanded.getExpansion());
    }

    private IBaseResource getResourceByType(Class<? extends IBaseResource> type, String resourceId, String versionId) {
        IBaseResource resource;
        try {
             resource = client
                .read()
                .resource(type)
                .withIdAndVersion(resourceId, versionId)
                .execute();
        } catch (ResourceNotFoundException e) {
            resource = null;
        }
        return resource;
    }

    private ICriterion<?> isActiveResource() {
        return new TokenClientParam(SP_STATUS).exactly().identifier(ACTIVE.toCode());
    }

    private DateTimeType getTimeUTC(Date lastUpdate) {
        return new DateTimeType(lastUpdate, MILLI, getTimeZone(UTC));
    }

}
