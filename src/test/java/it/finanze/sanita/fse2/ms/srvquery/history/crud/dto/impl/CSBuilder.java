package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.impl;

import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.IResBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Identifier;

import java.util.Date;

import static org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.*;

public class CSBuilder implements IResBuilder {

    private final CodeSystem cs;

    public CSBuilder(String oid) {
        this.cs = new CodeSystem();
        addIdentifier(oid);
        addVersion("1.0.0");
        addDate(new Date());
        addStatus(ACTIVE);
    }

    @Override
    public void addDate(Date date) {
        if(date != null) cs.setDate(date);
    }

    private CSBuilder(CodeSystem cs) {
        this.cs = cs;
    }

    public static CSBuilder from(CodeSystem cs) {
        return new CSBuilder(cs);
    }

    @Override
    public void addCodes(String code, String display) {
        // Prepare
        ConceptDefinitionComponent component = new ConceptDefinitionComponent();
        component.setCode(code);
        component.setDisplay(display);
        // Attach
        cs.getConcept().add(component);
    }

    @Override
    public void addIdentifier(String oid) {
        if(oid != null) {
            // Prepare
            Identifier id = new Identifier();
            id.setSystem(OID_REF);
            id.setValue(OID_PREFIX + oid);
            // Attach
            cs.getIdentifier().add(id);
        }
    }

    @Override
    public void addUrl(String url) {
        if(url != null) cs.setUrl(url);
    }

    @Override
    public void addVersion(String version) {
        if(version != null) cs.setVersion(version);
    }

    @Override
    public void addStatus(PublicationStatus status) {
        if(status != null) cs.setStatus(status);
    }

    @Override
    public CodeSystem build() {
        return cs;
    }
}
