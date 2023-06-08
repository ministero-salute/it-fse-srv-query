package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.ConverterClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ITerminologyCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CodeSystemsResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;

@RestController
public class TerminologyCTL extends AbstractCTL implements ITerminologyCTL {

    @Autowired
    private TerminologyClient terminology;

    @Autowired
    private ConverterClient converter;

    @Override
    public CodeSystemsResDTO getActiveCodeSystems() {
        List<CodeSystem> list = terminology.searchActiveCodeSystem();
        List<String> resources = new ArrayList<>();
        List<ConversionResponseDTO> converted = new ArrayList<>();

        for(CodeSystem codeSystem: list) {
            resources.add(FHIRR4Helper.serializeResource(codeSystem, true, false, false));
        }
        converted.addAll(converter.listToCsv(resources));

        return new CodeSystemsResDTO(converted.stream().map(x->x.getMessage()).toList());
    }
    
}
