package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TranslatorClient;
import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IMetadataResourceCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.MetadataResourceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.TranslateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.LanguageEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@RestController
public class MetadataResourceCTL extends AbstractCTL implements IMetadataResourceCTL {

	@Autowired
	private ITerminologySRV terminologySRV;

	@Value("${translator-server-url}")
    private String translatorServerURL;

	@Value("${terminology-server-url}")
    private String terminologyServerURL;

	@Value("${terminology-server-user}")
	private String terminologyServerUSR;
	
	@Value("${terminology-server-pwd}")
    private String terminologyServerPWD;
	
	@Override
	public MetadataResourceResponseDTO manageMetadataResource(List<SystemUrlDTO> requestBody) {
		List<MetadataResourceDTO> out = terminologySRV.manageMetadataResource(requestBody);
		return new MetadataResourceResponseDTO(getLogTraceInfo(), out);
	}

	@Override
	public TranslateResponseDTO translateCodeSystem(String id, LanguageEnum from, LanguageEnum to) {
    	TerminologyClient tc = new TerminologyClient(terminologyServerURL, terminologyServerUSR, terminologyServerPWD);
    	TranslatorClient txc = new TranslatorClient(translatorServerURL);
    	Boolean status = true;
    	String msg = null;
    	CodeSystem cs = null;
    	try {
        	cs = tc.readCS(id);
        	List<ConceptDefinitionComponent> concepts = translateConcepts(txc, cs.getConcept(), from, to);
        	cs.setConcept(concepts);
        	tc.updateCS(cs);
    	} catch (Exception e) {
    		status = false;
    		msg = e.getMessage();
    	}
		return new TranslateResponseDTO(getLogTraceInfo(), status, msg, cs);
	}

	private List<ConceptDefinitionComponent> translateConcepts(TranslatorClient txc, List<ConceptDefinitionComponent> concepts, LanguageEnum from, LanguageEnum to) {
    	for (ConceptDefinitionComponent cmc:concepts) {
    		Boolean bFound = false;
    		if (cmc.getDesignation()!=null) {
        		for (ConceptDefinitionDesignationComponent cddc:cmc.getDesignation()) {
        			if (to.getDescription().equals(cddc.getLanguage())) {
                		bFound = true;
        				break;
        			}
        		}
    		} else {
    			cmc.setDesignation(new ArrayList<ConceptDefinitionDesignationComponent>());
    		}
    		if (!bFound) {
    			ConceptDefinitionDesignationComponent cddc = new ConceptDefinitionDesignationComponent();
    			cddc.setLanguage(cddc.getLanguage());
    			cddc.setValue(txc.translate(cmc.getDisplay(), from.getCode(), to.getCode()).getTranslatedText());
    		}
    	}
    	return concepts;
	}

}
