package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types.CompactCS;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types.CompactVS;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.Optional;

import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static ca.uhn.fhir.rest.api.SummaryEnum.TRUE;
import static org.hl7.fhir.r4.model.ResourceType.*;
import static org.springframework.http.HttpMethod.POST;

public abstract class HistoryAbstractClient {

    private static final int CHUNK_SIZE = 10;

    protected final IGenericClient client;

    public HistoryAbstractClient(IGenericClient client) {
        this.client = client;
    }

    protected HistoryDTO createHistoryByLastUpdate(Date lastUpdate) {
        HistoryDTO out;
        if(lastUpdate == null) {
            out = createHistoryFromBegins();
        } else {
            out = createHistoryFromDate(lastUpdate);
        }
        return out;
    }

    private HistoryDTO createHistoryFromBegins() {
        // Execute query by resource op and last-update date
        Bundle bundle = client
            .search()
            .forResource(CodeSystem.class)
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
        return new HistoryDTO(
            currentTime,
            null,
            composer.compose(POST, null)
        );
    }

    private HistoryDTO createHistoryFromDate(Date lastUpdate) {
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
        return new HistoryDTO(
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
        ResourceType type = fromCode(resource.fhirType());
        // Start mapping
        if(type == CodeSystem) {
            res = new CompactCS(resourceId, versionId, (CodeSystem) resource).convert();
        } else if(type == ValueSet) {
            res = new CompactVS(resourceId, versionId, (ValueSet) resource).convert();
        }else {
            throw new IllegalArgumentException("Unknown type to map: " + type);
        }
        return res;
    }

    private Optional<IBaseResource> getResource(String resourceId, String versionId) {
        IBaseResource out = getResourceByType(CodeSystem.class, resourceId, versionId);
        if(out == null) out = getResourceByType(ValueSet.class, resourceId, versionId);
        return Optional.ofNullable(out);
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
