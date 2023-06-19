package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.HttpMethod;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.*;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO.*;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.*;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.INSERT;
import static org.hl7.fhir.r4.model.Bundle.*;

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
            bundle = HistoryUtils.hasNextPage(client, bundle);
        }
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
                // Register then return id
                String id = registerResIfAbsent(res, type);
                // If we reached the POST operation of a DELETED resource
                if(isLastOperation(id, DELETE) && type == INSERT) {
                    omitResourceExistedInBetween(res, id, lastUpdate);
                }
                // If we reached the POST operation of an UPDATED resource
                else if(isLastOperation(id, UPDATE) && type == INSERT) {
                    reviseResourceUpsertInBetween(res, id, lastUpdate);
                }
            } else {
                // For deleted resource getResource() returns null,
                // so we return the id from getFullUrl
                registerDeletedResIfAbsent(entry, type);
            }
        }
    }

    private void reviseResourceUpsertInBetween(Resource res, String id, Date lastUpdate) {
        // Retrieve creation date
        Date insertionDate = res.getMeta().getLastUpdated();
        // If defined and updated in between the lastUpdate, define as INSERT instead of UPDATE
        if(insertionDate.after(lastUpdate)) {
            // Replace it
            String root = map.get(id).getVersion();
            map.replace(id, new HistoryDetailsDTO(root, INSERT));
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
        map.putIfAbsent(HistoryUtils.asId(entry.getFullUrl()), new HistoryDetailsDTO(ANY_VERSION, type));
    }

    private String registerResIfAbsent(Resource res, HistoryOperationEnum type) {
        // Get id
        String id = HistoryUtils.asId(res);
        // Register operation op
        map.putIfAbsent(id, new HistoryDetailsDTO(HistoryUtils.asVersionId(res), type));
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
