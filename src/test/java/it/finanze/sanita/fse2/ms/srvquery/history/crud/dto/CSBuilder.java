package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Identifier;

import static org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;

public class CSBuilder {

    private static final String OID_REF = "urn:ietf:rfc:3986";
    private static final String OID_PREFIX = "urn:oid:";

    private final CodeSystem cs;

    public CSBuilder(String oid) {
        this.cs = new CodeSystem();
        addIdentifier(oid);
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

    public CodeSystem build() {
        return cs;
    }
}
