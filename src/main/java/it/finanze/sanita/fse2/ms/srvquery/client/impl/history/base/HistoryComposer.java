package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.HttpMethod;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryUtils.*;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO.ANY_VERSION;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.*;
import static org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;

public class HistoryComposer {

    private final IGenericClient client;
    private Bundle bundle;
    private final Map<String, HistoryDetailsDTO> map;

    public HistoryComposer(IGenericClient client, Bundle bundle) {
        this.client = client;
        this.bundle = bundle;
        this.map = new HashMap<>();
    }

    public Map<String, HistoryDetailsDTO> compose(@Nullable HttpMethod method, Date lastUpdate) {
        // Iterate for each page of the bundle
        while (bundle != null) {
            // Add resources
            extract(method, lastUpdate);
            // Verify if next page is available
            bundle = hasNextPage(client, bundle);
        }
        // Omit draft resources
        omitDraftResourcesFromHistory();
        // Now returns
        return map;
    }

    private void extract(@Nullable HttpMethod op, Date lastUpdate) {
        // Retrieve entries
        List<BundleEntryComponent> resources = bundle.getEntry();
        // Iterate
        for (BundleEntryComponent entry : resources) {
            // Get resource
            Resource res = entry.getResource();
            // Obtain display value
            HistoryOperationEnum type = getHistoryOperation(op, entry);
            // Check if resource deleted
            if(res != null) {
                // Get status
                PublicationStatus status = getPublicationStatus(res);
                // Register then return id
                String id = registerResIfAbsent(res, type, status);
                // If we reached the POST operation of a DELETED resource
                if(isLastOperation(id, DELETE) && type == INSERT) {
                    omitResourceExistedInBetween(res, id, lastUpdate);
                }
                // If we reached the POST operation of an UPDATED resource
                else if(isLastOperation(id, UPDATE) && type == INSERT) {
                    reviseResourceUpsertInBetween(res, id, lastUpdate, status);
                }
                // If we reached the PUT/PATCH operation of an UPDATED resource
                else if(isLastOperation(id, UPDATE) && type == UPDATE) {
                    reviseResourceFromDraftToActive(res, id);
                }
            } else {
                // For deleted resource getResource() returns null,
                // so we return the id from getFullUrl
                registerDeletedResIfAbsent(entry, type);
            }
        }
    }

    private void omitDraftResourcesFromHistory() {
        List<String> keys = map
            .keySet()
            .stream()
            .filter(id -> map.get(id).getStatus() != ACTIVE)
            .collect(Collectors.toList());
        keys.forEach(map::remove);
    }

    private void reviseResourceFromDraftToActive(Resource res, String id) {
        // Skip if resource is not active
        if(map.get(id).getStatus() == ACTIVE) {
            // Verify if previous one was different from active
            PublicationStatus previous = getPreviousPublicationStatus(res, id);
            // Check
            if(previous != ACTIVE) {
                // Register as an insertion
                String root = map.get(id).getVersion();
                // Replace it
                map.replace(id, new HistoryDetailsDTO(root, INSERT, ACTIVE));
            }
        }
    }

    private PublicationStatus getPreviousPublicationStatus(Resource res, String id) {
        // Map as integer, then subtract to get previous version
        int version = Integer.parseInt(asVersionId(res)) - 1;
        // Retrieve latest version
        Resource raw = (Resource) client
            .read()
            .resource(res.fhirType())
            .withIdAndVersion(id, String.valueOf(version))
            .summaryMode(SummaryEnum.TRUE)
            .cacheControl(noCache())
            .preferResponseType(Resource.class)
            .execute();
        // Get status
        return getPublicationStatus(raw);
    }

    private void reviseResourceUpsertInBetween(Resource res, String id, Date lastUpdate, PublicationStatus status) {
        // Retrieve creation date
        Date insertionDate = res.getMeta().getLastUpdated();
        // If defined and updated in between the lastUpdate, define as INSERT instead of UPDATE
        if(insertionDate.after(lastUpdate)) {
            // Get root
            String version = map.get(id).getVersion();
            // Replace it
            map.replace(id, new HistoryDetailsDTO(version, INSERT, status));
        }
    }

    private void omitResourceExistedInBetween(Resource res, String id, Date lastUpdate) {
        // Retrieve creation date
        Date insertionDate = res.getMeta().getLastUpdated();
        // If defined and deleted in between the lastUpdate, throw it away
        if(insertionDate.after(lastUpdate)) {
            // Remove it
            map.remove(id);
        }
    }

    private boolean isLastOperation(String id, HistoryOperationEnum op) {
        return map.get(id).getOp() == op;
    }

    private void registerDeletedResIfAbsent(BundleEntryComponent entry, HistoryOperationEnum type) {
        // TODO Decide how to define deleted resources as status, or if one should use the previous value
        // TODO Remember any status != ACTIVE will be erased from the history
        map.putIfAbsent(asId(entry.getFullUrl()), new HistoryDetailsDTO(ANY_VERSION, type, ACTIVE));
    }

    private String registerResIfAbsent(
        Resource res,
        HistoryOperationEnum type,
        PublicationStatus status
    ) {
        // Get id
        String id = asId(res);
        // Register operation op
        map.putIfAbsent(id, new HistoryDetailsDTO(asVersionId(res), type, status));
        // Return id
        return id;
    }

    private static HistoryOperationEnum getHistoryOperation(@Nullable HttpMethod op, BundleEntryComponent entry) {
        // Working var
        HistoryOperationEnum type;
        // Let me know the latest operation op
        HTTPVerb method = entry.getRequest().getMethod();
        // Override if provided
        if(op != null) {
            type = parseHistoryOp(op.name());
        } else {
            type = parseHistoryOp(method.getDisplay());
        }
        return type;
    }

}
