package it.finanze.sanita.fse2.ms.srvquery.history.crud;

import static it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryUtils.asId;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class FhirCrudClient {

    private final IGenericClient client;

    public FhirCrudClient(String uri, String user, String pwd) {
        this.client = createClient(uri, user, pwd);
    }

    public <T extends IBaseResource> String createResource(T resource) {
        MethodOutcome outcome = client.create().resource(resource).execute();
        return asId(outcome.getResource());
    }

    public <T extends IBaseResource> T readResource(String id, Class<T> clazz) {
        return client.read().resource(clazz).withId(id).execute();
    }

    public <T extends IBaseResource> T readResource(String id, String version, Class<T> clazz) {
        return client.read().resource(clazz).withIdAndVersion(id, version).execute();
    }

    public <T extends IBaseResource> void updateResource(T resource) {
    	resource.setId(new IdType(resource.fhirType(), asId(resource)));
        client.update().resource(resource).execute();
    }

    public <T extends IBaseResource> void deleteResource(String id, Class<T> clazz) {
        deleteResource(readResource(id, clazz));
    }

    public <T extends IBaseResource> void deleteResource(T resource) {
        client.delete().resource(resource).execute();
    }

    public void reset() {
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
