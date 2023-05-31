package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateRangeParam;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.List;

import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.asId;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.getResources;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;

public class DiffClient {

    private final IGenericClient client;

    public DiffClient(String uri, String user, String pwd) {
        this.client = createClient(uri, user, pwd);
    }

    public List<String> findByLastUpdate(Date lastUpdate, Class<? extends MetadataResource> clazz) {
        List<String> out;
        if(lastUpdate == null) {
            out = findAny(clazz);
        } else {
            out = findModifiedByDate(lastUpdate, clazz);
        }
        return out;
    }

    private List<String> findAny(Class<? extends MetadataResource> clazz) {
        // Execute query by resource type and last-update date
        Bundle bundle = client
            .search()
            .forResource(clazz)
            .returnBundle(Bundle.class)
            .execute();

        // Retrieve resources
        List<Resource> resources = getResources(bundle);
        // Map
        return asId(resources);
    }

    private List<String> findModifiedByDate(Date lastUpdate, Class<? extends MetadataResource> clazz) {
        // Execute query by resource type and last-update date
        Bundle bundle = client
            .search()
            .forResource(clazz)
            // Range is treated inclusively
            .lastUpdated(new DateRangeParam(lastUpdate, null))
            .returnBundle(Bundle.class)
            .execute();
        // Retrieve resources
        List<Resource> resources = getResources(bundle);
        // Map
        return asId(resources);
    }

    public void resetFhir() {
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
