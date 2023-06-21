package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.composer;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.*;
import java.util.stream.Collectors;

import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryUtils.*;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.*;
import static org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;

public class HistoryComposer {

    private final IGenericClient client;
    private final Bundle[] bundles;
    private final Map<String, ArrayDeque<HistoryDetailsDTO>> composition;

    public HistoryComposer(IGenericClient client, Bundle ...bundle) {
        this.client = client;
        this.bundles = bundle;
        this.composition = new HashMap<>();
    }

    public Map<String, HistoryDetailsDTO> compose() {
        for (Bundle bundle : bundles) {
            // Sort and organise data for each bundle
            createComposition(bundle);
        }
        // Refine data
        refineComposition();
        // Now returns
        return convert();
    }

    private Map<String, HistoryDetailsDTO> convert() {
        // Working var
        Map<String, HistoryDetailsDTO> out = new HashMap<>();
        // Retrieve last change for everybody
        composition.forEach((id, details) -> out.put(id, details.getFirst()));
        // Return
        return out;
    }

    private void createComposition(Bundle bundle) {
        // For each page of the bundle
        while (bundle != null) {
            // For each entry of the page of the bundle
            for (BundleEntryComponent entry : bundle.getEntry()) {
                // Analyze entry and create representation
                createDetails(entry);
            }
            // Verify if next page is available
            bundle = hasNextPage(client, bundle);
        }
    }

    private void createDetails(BundleEntryComponent entry) {
        String id;
        String version;
        ResourceType type;
        PublicationStatus status;
        Date lastUpdated = null;
        // Retrieve operation type (INSERT/UPDATE/DELETE)
        HistoryOperationEnum op = asHistoryOperation(entry);
        // Retrieve id and version
        if(op == DELETE) {
            id = ResourceDeleted.extractIdFromFullUrl(entry.getFullUrl());
            version = ResourceDeleted.extractVersionIdFromRequestUrl(entry.getRequest().getUrl());
            type = ResourceDeleted.extractFhirTypeFromRequestUrl(entry.getRequest().getUrl());
            status = getPreviousPublicationStatus(id, version, type);
        } else {
            id = asId(entry.getResource());
            version = asVersionId(entry.getResource());
            type = entry.getResource().getResourceType();
            status = asStatus(entry.getResource());
            lastUpdated = entry.getResource().getMeta().getLastUpdated();
        }
        // Now after we got the details of the current operation, we update the list
        HistoryDetailsDTO detail = new HistoryDetailsDTO(type, version, op, status, lastUpdated);
        // Check if it should be inserted
        if(composition.containsKey(id)) {
            // Retrieve it
            ArrayDeque<HistoryDetailsDTO> details = composition.get(id);
            // Add it
            details.add(detail);
        } else {
            // Create it
            ArrayDeque<HistoryDetailsDTO> details = new ArrayDeque<>();
            // Add it
            details.add(detail);
            // Insert
            composition.put(id, details);
        }
    }

    private void refineComposition() {
        // Remove resources in-draft
        omitDraftResourcesFromHistory();
        // Remove resources born as active but then deactivated
        omitActiveResourcesInBetween();
        // Remove resources created and deleted in between
        omitResourceExistedInBetween();
        // Re-write as insertion resources that were created and updated in between
        reviseResourceUpsertInBetween();
        // Re-write as insertion resources that went from draft to active
        reviseResourceFromDeactivateToActive();
        // Re-write as deletions resources that went from active to draft
        reviseResourceFromActiveToDeactivate();
    }

    private void omitDraftResourcesFromHistory() {
        // Remove pure draft resources
        Set<String> keys = composition
            .keySet()
            .stream()
            .filter(this::isNotAnActiveResource)
            .filter(this::wasDeactivateResourceBeforeChanges)
            .collect(Collectors.toSet());
        // Remove
        keys.forEach(composition::remove);
    }

    private void omitActiveResourcesInBetween() {
        // Get ids
        List<String> ids = new ArrayList<>(composition.keySet());
        // For each one, retrieve resource
        for (String id : ids) {
            // Verify condition
            ArrayDeque<HistoryDetailsDTO> details = composition.get(id);
            // Get operations
            HistoryDetailsDTO latest = details.getFirst();
            HistoryDetailsDTO first = details.getLast();
            // Check
            if(
                latest.getStatus() != ACTIVE &&
                first.getStatus() == ACTIVE &&
                first.getOp() == INSERT
            ) {
                // Clear history
                composition.get(id).clear();
                // Remove
                composition.remove(id);
            }
        }
    }

    private void omitResourceExistedInBetween() {
        // Get ids
        List<String> ids = new ArrayList<>(composition.keySet());
        // For each one, retrieve resource
        for (String id : ids) {
            // Verify condition
            ArrayDeque<HistoryDetailsDTO> details = composition.get(id);
            // Get operations
            HistoryDetailsDTO latest = details.getFirst();
            HistoryDetailsDTO first = details.getLast();
            // Check
            if(
                latest.getOp() == DELETE &&
                latest.getStatus() == ACTIVE &&
                first.getOp() == INSERT
            ) {
                // Clear history
                composition.get(id).clear();
                // Remove
                composition.remove(id);
            }
        }
    }

    private void reviseResourceUpsertInBetween() {
        // Get ids
        List<String> ids = new ArrayList<>(composition.keySet());
        // For each one, retrieve resource
        for (String id : ids) {
            // Verify condition
            ArrayDeque<HistoryDetailsDTO> details = composition.get(id);
            // Get operations
            HistoryDetailsDTO latest = details.getFirst();
            HistoryDetailsDTO first = details.getLast();
            // Check
            if(
                latest.getOp() == UPDATE &&
                latest.getStatus() == ACTIVE &&
                first.getOp() == INSERT
            ) {
                // Clear history
                composition.get(id).clear();
                // Map as insertion
                composition.get(id).add(HistoryDetailsDTO.from(latest, INSERT));
            }
        }
    }

    private void reviseResourceFromDeactivateToActive() {
        // Get ids
        List<String> ids = new ArrayList<>(composition.keySet());
        // For each one, retrieve resource
        for (String id : ids) {
            // Verify condition
            ArrayDeque<HistoryDetailsDTO> details = composition.get(id);
            // Get operations
            HistoryDetailsDTO latest = details.getFirst();
            // Check
            if(
                latest.getOp() == UPDATE &&
                latest.getStatus() == ACTIVE &&
                wasDraftResource(id)
            ) {
                // Clear history
                composition.get(id).clear();
                // Map as insertion
                composition.get(id).add(HistoryDetailsDTO.from(latest, INSERT));
            }
        }
    }

    private void reviseResourceFromActiveToDeactivate() {
        // Get ids
        List<String> ids = new ArrayList<>(composition.keySet());
        // For each one, retrieve resource
        for (String id : ids) {
            // Verify condition
            ArrayDeque<HistoryDetailsDTO> details = composition.get(id);
            // Get operations
            HistoryDetailsDTO latest = details.getFirst();
            // Check
            if(
                latest.getOp() == UPDATE &&
                latest.getStatus() != ACTIVE &&
                wasActiveResource(id)
            ) {
                // Clear history
                composition.get(id).clear();
                // Map as insertion
                composition.get(id).add(HistoryDetailsDTO.from(latest, DELETE));
            }
        }
    }


    private PublicationStatus getPreviousPublicationStatus(String id, String version, ResourceType type) {
        // Map as integer, then subtract to get previous version
        int previous = Integer.parseInt(version) - 1;
        // Integrity check
        if(previous == 0) {
            throw new IllegalArgumentException("Cannot search for version zero, this is a bug");
        }
        // Retrieve latest version
        Resource raw = (Resource) client
            .read()
            .resource(type.getPath())
            .withIdAndVersion(id, String.valueOf(previous))
            .summaryMode(SummaryEnum.TRUE)
            .cacheControl(noCache())
            .preferResponseType(Resource.class)
            .execute();
        // Get status
        return asStatus(raw);
    }

    private boolean isNotAnActiveResource(String id) {
        ArrayDeque<HistoryDetailsDTO> history = composition.get(id);
        HistoryDetailsDTO latest = history.getFirst();
        HistoryDetailsDTO earliest = history.getLast();
        return latest.getStatus() != ACTIVE && earliest.getStatus() != ACTIVE;
    }

    private boolean wasDraftResource(String id) {
        return wasDeactivateResourceInHistory(id) || wasDeactivateResourceBeforeChanges(id);
    }

    private boolean wasActiveResource(String id) {
        return wasActivateResourceInHistory(id) || wasActivateResourceBeforeChanges(id);
    }

    private boolean wasActivateResourceInHistory(String id) {
        return composition
            .get(id)
            .stream()
            .anyMatch(v -> v.getStatus() == ACTIVE);
    }

    private boolean wasDeactivateResourceInHistory(String id) {
        return composition
            .get(id)
            .stream()
            .anyMatch(v -> v.getStatus() != ACTIVE);
    }

    private boolean wasDeactivateResourceBeforeChanges(String id) {
        // Working var
        Resource raw = null;
        // Get the earliest tracked version
        HistoryDetailsDTO dto = composition.get(id).getLast();
        // Map as integer, then subtract to get previous version
        int previous = Integer.parseInt(dto.getVersion()) - 1;
        // Skip if previous is zero
        if(previous != 0) {
            // Retrieve latest version
            raw = (Resource) client
                .read()
                .resource(dto.getType().getPath())
                .withIdAndVersion(id, String.valueOf(previous))
                .summaryMode(SummaryEnum.TRUE)
                .cacheControl(noCache())
                .preferResponseType(Resource.class)
                .execute();
        }
        // Get status
        return raw != null ? asStatus(raw) != ACTIVE : dto.getStatus() != ACTIVE;
    }
    private boolean wasActivateResourceBeforeChanges(String id) {
        // Working var
        Resource raw = null;
        // Get the earliest tracked version
        HistoryDetailsDTO dto = composition.get(id).getLast();
        // Map as integer, then subtract to get previous version
        int previous = Integer.parseInt(dto.getVersion()) - 1;
        // Skip if previous is zero
        if(previous != 0) {
            // Retrieve latest version
            raw = (Resource) client
                .read()
                .resource(dto.getType().getPath())
                .withIdAndVersion(id, String.valueOf(previous))
                .summaryMode(SummaryEnum.TRUE)
                .cacheControl(noCache())
                .preferResponseType(Resource.class)
                .execute();
        }
        // Get status
        return raw != null ? asStatus(raw) == ACTIVE : dto.getStatus() == ACTIVE;
    }


}
