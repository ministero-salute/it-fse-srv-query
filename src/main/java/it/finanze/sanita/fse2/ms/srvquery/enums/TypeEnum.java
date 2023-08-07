package it.finanze.sanita.fse2.ms.srvquery.enums;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.ValueSet;


public enum TypeEnum {
    CODE_SYSTEM(CodeSystem.class),
    VALUE_SET(ValueSet.class);

    private Class<? extends MetadataResource> metadataResourceClass;

    TypeEnum(Class<? extends MetadataResource> metadataResourceClass) {
        this.metadataResourceClass = metadataResourceClass;
    }

    public Class<? extends MetadataResource> getMetadataResourceClass() {
        return metadataResourceClass;
    }
}