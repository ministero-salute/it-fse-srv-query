package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types.CompactCS;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types.CompactVS;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.Optional;

import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static ca.uhn.fhir.rest.api.SummaryEnum.TRUE;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.springframework.http.HttpMethod.POST;

public abstract class HistoryAbstractClient {

    private static final int CHUNK_SIZE = 10;

    protected final IGenericClient client;

    public HistoryAbstractClient(IGenericClient client) {
        this.client = client;
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
        // Execute query by resource op and last-update date
        Bundle bundle = client
            .search()
            .forResource(CodeSystem.class)
            .where(isActiveCS())
            .cacheControl(noCache())
            .returnBundle(Bundle.class)
            .count(CHUNK_SIZE)
            .summaryMode(TRUE)
            .execute();
        // Create composer
        HistoryComposer composer = new HistoryComposer(client, bundle);
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new RawHistoryDTO(
            currentTime,
            null,
            composer.compose(POST, null)
        );
    }

    private RawHistoryDTO createHistoryFromDate(Date lastUpdate) {
        // Execute query by resource op and last-update date
        Bundle bundle = client
            .history()
            .onType(CodeSystem.class)
            .returnBundle(Bundle.class)
            .cacheControl(noCache())
            .since(lastUpdate)
            .summaryMode(TRUE)
            .count(CHUNK_SIZE)
            .execute();
        // Create composer
        HistoryComposer composer = new HistoryComposer(client, bundle);
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new RawHistoryDTO(
            currentTime,
            lastUpdate,
            composer.compose(null, lastUpdate)
        );
    }

    protected Optional<HistoryResourceDTO> getMappedResource(String resourceId, String versionId) throws MalformedResourceException {
        // Working var
        HistoryResourceDTO out = null;
        // Get resource
        Optional<IBaseResource> wrapper = getResource(resourceId, versionId);
        // Check if found
        if(wrapper.isPresent()) {
            // Retrieve resource
            out = mapResource(wrapper.get(), resourceId, versionId);
        }
        return Optional.ofNullable(out);
    }

    private HistoryResourceDTO mapResource(IBaseResource resource, String resourceId, String versionId) throws MalformedResourceException {
        // Working var
        HistoryResourceDTO res;
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

    private ICriterion<?> isActiveCS() {
        return CodeSystem.STATUS.exactly().identifier(ACTIVE.getDisplay());
    }

    protected void reset() {
        // Inside terminology-server (AppProperties.java):
        // allow_multiple_delete = true
        client
            .operation()
            .onServer()
            .named("$expunge")
            .withParameter(
                Parameters.class, "expungeEverything", new BooleanType(true)
            ).execute();
    }

}
