package it.finanze.sanita.fse2.ms.srvquery.history.base;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.BaseResource;

@RequiredArgsConstructor
public class TestResource {

    private final String name;
    private final BaseResource resource;

    public BaseResource resource() {
        return resource;
    }
    public String name() {
        return name;
    }

    public Class<? extends BaseResource> type() {
        return resource.getClass();
    }
}
