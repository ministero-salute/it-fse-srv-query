package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Identifier;

import java.util.Date;

import static org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.*;

public class CSBuilder {

    private static final String OID_REF = "urn:ietf:rfc:3986";
    private static final String OID_PREFIX = "urn:oid:";

    private final CodeSystem cs;

    public CSBuilder(String oid) {
        this.cs = new CodeSystem();
        addIdentifier(oid);
        addVersion("1.0.0");
        addDate(new Date());
        addStatus(ACTIVE);
    }

    private void addDate(Date date) {
        if(date != null) cs.setDate(date);
    }

    private CSBuilder(CodeSystem cs) {
        this.cs = cs;
    }

    public static CSBuilder from(CodeSystem cs) {
        return new CSBuilder(cs);
    }

    public void addCodes(String code, String display) {
        // Prepare
        ConceptDefinitionComponent component = new ConceptDefinitionComponent();
        component.setCode(code);
        component.setDisplay(display);
        // Attach
        cs.getConcept().add(component);
    }

    private void addIdentifier(String oid) {
        if(oid != null) {
            // Prepare
            Identifier id = new Identifier();
            id.setSystem(OID_REF);
            id.setValue(OID_PREFIX + oid);
            // Attach
            cs.getIdentifier().add(id);
        }
    }

    public void addUrl(String url) {
        if(url != null) cs.setUrl(url);
    }

    public void addVersion(String version) {
        if(version != null) cs.setVersion(version);
    }

    public void addStatus(PublicationStatus status) {
        if(status != null) cs.setStatus(status);
    }

    public CodeSystem build() {
        return cs;
    }
}
