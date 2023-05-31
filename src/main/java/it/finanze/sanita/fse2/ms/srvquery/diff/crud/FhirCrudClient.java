package it.finanze.sanita.fse2.ms.srvquery.diff.crud;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;

import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.asId;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;

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

    public <T extends IBaseResource> void updateResource(T resource) {
        client.update().resource(resource).execute();
    }

    public <T extends IBaseResource> void deleteResource(String id, Class<T> clazz) {
        client.delete().resource(readResource(id, clazz)).execute();
    }

    public <T extends IBaseResource> void deleteResource(T resource) {
        client.delete().resource(resource).execute();
    }

}
