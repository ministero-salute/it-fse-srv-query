package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.impl;

import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.IResBuilder;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Date;

import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;

public class VSBuilder implements IResBuilder {

    private final ValueSet vs;

    public VSBuilder(String oid, String url) {
        this.vs = new ValueSet();
        addUrl(url);
        addIdentifier(oid);
        addVersion("1.0.0");
        addDate(new Date());
        addStatus(ACTIVE);
    }

    @Override
    public void addDate(Date date) {
        if(date != null) vs.setDate(date);
    }

    @Override
    public void addUrl(String url) {
        vs.setUrl(url);
    }

    private VSBuilder(ValueSet vs) {
        this.vs = vs;
    }

    public static VSBuilder from(ValueSet vs) {
        return new VSBuilder(vs);
    }

    @Override
    public void addCodes(String code, String display) {
        // Prepare
        ValueSetExpansionContainsComponent component = new ValueSetExpansionContainsComponent();
        component.setCode(code);
        component.setDisplay(display);
        // Attach
        vs.getExpansion().getContains().add(component);
    }

    @Override
    public void addIdentifier(String oid) {
        if(oid != null) {
            // Prepare
            Identifier id = new Identifier();
            id.setSystem(OID_REF);
            id.setValue(OID_PREFIX + oid);
            // Attach
            vs.getIdentifier().add(id);
        }
    }

    @Override
    public void addVersion(String version) {
        if(version != null) vs.setVersion(version);
    }

    public void addInclusionCS(String uri) {
        vs.getCompose().getInclude().add(new ValueSet.ConceptSetComponent().setSystem(uri));
    }

    @Override
    public void addStatus(Enumerations.PublicationStatus status) {
        if(status != null) vs.setStatus(status);
    }

    @Override
    public ValueSet build() {
        return vs;
    }
}
