package it.finanze.sanita.fse2.ms.srvquery.diff.base;

import it.finanze.sanita.fse2.ms.srvquery.diff.crud.dto.CSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;

public abstract class AbstractTestResources {
    protected CodeSystem createGenderTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.5.1");
        builder.addCodes("M", "Male");
        builder.addCodes("F", "Female");
        return builder.build();
    }

    protected CodeSystem createOreTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.2.9.6.1.54.6");
        builder.addCodes("P", "Platinum");
        builder.addCodes("D", "Diamond");
        builder.addCodes("G", "Gold");
        return builder.build();
    }
}
