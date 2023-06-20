package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto;

import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.impl.CSBuilder;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.impl.VSBuilder;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ValueSet;

public class RSBuilder {

    public static IResBuilder<?> from(BaseResource res) {
        IResBuilder<? extends BaseResource> builder;
        if(res instanceof ValueSet) {
            builder = VSBuilder.from((ValueSet) res);
        } else if (res instanceof CodeSystem) {
            builder = CSBuilder.from((CodeSystem) res);
        } else {
            throw new IllegalArgumentException("Unknown instance type");
        }
        return builder;
    }
}
