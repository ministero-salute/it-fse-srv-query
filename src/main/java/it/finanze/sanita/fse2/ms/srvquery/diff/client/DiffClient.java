package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.Map;

import static ca.uhn.fhir.context.FhirVersionEnum.*;
import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static ca.uhn.fhir.rest.api.SummaryEnum.*;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.mapResourcesAs;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.mapResourcesAsHistory;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;
import static org.springframework.http.HttpMethod.POST;

public class DiffClient {

    private static final int CHUNK_SIZE = 10;

    private final IGenericClient client;

    public DiffClient(String uri, String user, String pwd) {
        this.client = createClient(uri, user, pwd);
    }

    public Map<String, DiffOpType> getChangesetCS(Date lastUpdate, boolean debug) {
        return findByLastUpdate(lastUpdate, CodeSystem.class, debug).changeset();
    }

    public Map<String, DiffOpType> getChangesetCS(Date lastUpdate) {
        return findByLastUpdate(lastUpdate, CodeSystem.class, false).changeset();
    }

    private DiffResult findByLastUpdate(Date lastUpdate, Class<? extends MetadataResource> clazz, boolean debug) {
        DiffResult out;
        if(lastUpdate == null) {
            out = findAny(clazz, debug);
        } else {
            out = findModifiedByDate(lastUpdate, clazz, debug);
        }
        return out;
    }

    private DiffResult findAny(Class<? extends MetadataResource> clazz, boolean debug) {
        // Execute query by resource type and last-update date
        Bundle bundle = client
            .search()
            .forResource(clazz)
            .cacheControl(noCache())
            .returnBundle(Bundle.class)
            .count(CHUNK_SIZE)
            .summaryMode(TRUE)
            .execute();
        // Print result
        if(debug) printJSON(bundle);
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new DiffResult(
            currentTime,
            null,
            mapResourcesAs(client, bundle, POST, null)
        );
    }

    private DiffResult findModifiedByDate(Date lastUpdate, Class<? extends MetadataResource> clazz, boolean debug) {
        // Execute query by resource type and last-update date
        Bundle bundle = client
            .history()
            .onType(clazz)
            .returnBundle(Bundle.class)
            .cacheControl(noCache())
            .since(lastUpdate)
            .summaryMode(TRUE)
            .count(CHUNK_SIZE)
            .execute();
        // Print result
        if (debug) printJSON(bundle);
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new DiffResult(
            currentTime,
            lastUpdate,
            mapResourcesAsHistory(client, bundle, lastUpdate)
        );
    }

    private void printJSON(Bundle bundle) {
        FhirContext context = FhirContext.forCached(R4);
        IParser parser = context.newJsonParser();
        parser.setPrettyPrint(true);
        System.out.println(parser.encodeResourceToString(bundle));
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
