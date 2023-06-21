package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.composer;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryUtils.*;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.INSERT;
import static org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

public class SimpleComposer {

    private final IGenericClient client;
    private final Bundle[] bundles;
    private final Map<String, HistoryDetailsDTO> composition;

    public SimpleComposer(IGenericClient client, Bundle ...bundle) {
        this.client = client;
        this.bundles = bundle;
        this.composition = new HashMap<>();
    }

    public Map<String, HistoryDetailsDTO> compose() {
        for (Bundle bundle : bundles) {
            // Sort and organise data for each bundle
            createComposition(bundle);
        }
        // Now returns
        return composition;
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
        // Retrieve id and version
        String id = asId(entry.getResource());
        String version = asVersionId(entry.getResource());
        ResourceType type = entry.getResource().getResourceType();
        PublicationStatus status = asStatus(entry.getResource());
        Date lastUpdated = entry.getResource().getMeta().getLastUpdated();
        // Now after we got the details of the current operation, we update the list
        HistoryDetailsDTO detail = new HistoryDetailsDTO(type, version, INSERT, status, lastUpdated);
        // Insert
        composition.putIfAbsent(id, detail);
    }

}
