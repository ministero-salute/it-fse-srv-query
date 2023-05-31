package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Parameters;

import java.util.Date;
import java.util.List;

import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.getResources;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;

public class DiffClient {

    private final IGenericClient client;

    public DiffClient(String uri, String user, String pwd) {
        this.client = createClient(uri, user, pwd);
    }

    public DiffResult findByLastUpdate(Date lastUpdate, Class<? extends MetadataResource> clazz) {
        DiffResult out;
        if(lastUpdate == null) {
            out = findAny(clazz);
        } else {
            out = findModifiedByDate(lastUpdate, clazz);
        }
        return out;
    }

    private DiffResult findAny(Class<? extends MetadataResource> clazz) {
        // Execute query by resource type and last-update date
        Bundle bundle = client
            .search()
            .forResource(clazz)
            .cacheControl(noCache())
            .returnBundle(Bundle.class)
            .execute();
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new DiffResult(currentTime,null, getResources(client, bundle));
    }

    private DiffResult findModifiedByDate(Date lastUpdate, Class<? extends MetadataResource> clazz) {
        // Execute query by resource type and last-update date
        Bundle bundle = client
            .history()
            .onType(clazz)
            .returnBundle(Bundle.class)
            .cacheControl(noCache())
            .since(lastUpdate)
            .execute();
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new DiffResult(currentTime, lastUpdate, getResources(client, bundle));
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