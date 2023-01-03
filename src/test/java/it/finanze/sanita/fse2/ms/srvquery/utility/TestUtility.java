package it.finanze.sanita.fse2.ms.srvquery.utility;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;

public class TestUtility {
    private TestUtility() {}

    public static Bundle mockTestBundle() {
        Bundle bundle = new Bundle();
        bundle.setIdElement(new IdType().setValue("value"));
        return bundle;
    }
}
