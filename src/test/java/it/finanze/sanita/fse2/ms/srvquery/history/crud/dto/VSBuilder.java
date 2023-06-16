package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Date;

import static org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;

public class VSBuilder {

    private static final String OID_REF = "urn:ietf:rfc:3986";
    private static final String OID_PREFIX = "urn:oid:";

    private final ValueSet vs;

    public VSBuilder(String oid, String url) {
        this.vs = new ValueSet();
        addUrl(url);
        addIdentifier(oid);
        addVersion("1.0.0");
        addDate(new Date());
    }

    private void addDate(Date date) {
        if(date != null) vs.setDate(date);
    }

    private void addUrl(String url) {
        vs.setUrl(url);
    }

    private VSBuilder(ValueSet vs) {
        this.vs = vs;
    }

    public static VSBuilder from(ValueSet vs) {
        return new VSBuilder(vs);
    }

    public void addCodes(String code, String display) {
        // Prepare
        ValueSetExpansionContainsComponent component = new ValueSetExpansionContainsComponent();
        component.setCode(code);
        component.setDisplay(display);
        // Attach
        vs.getExpansion().getContains().add(component);
    }

    private void addIdentifier(String oid) {
        if(oid != null) {
            // Prepare
            Identifier id = new Identifier();
            id.setSystem(OID_REF);
            id.setValue(OID_PREFIX + oid);
            // Attach
            vs.getIdentifier().add(id);
        }
    }

    public void addVersion(String version) {
        if(version != null) vs.setVersion(version);
    }

    public void addInclusionCS(String uri) {
        vs.getCompose().getInclude().add(new ValueSet.ConceptSetComponent().setSystem(uri));
    }

    public ValueSet build() {
        return vs;
    }
}
