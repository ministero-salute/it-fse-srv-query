/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentRelationshipType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Service Implementation 
 *
 */
@Service
@Slf4j
public class FHIRSRV implements IFHIRSRV {

    @Autowired
	private FhirCFG fhirCFG;

    private FHIRClient fhirClient;

    @PostConstruct
    void init() {
    	fhirClient = new FHIRClient(fhirCFG.getFhirServerUrl());
    }

    @Override
    public boolean create(final FhirPublicationDTO createDTO) {
    	String json = createDTO.getJsonString();
    	Bundle bundle = deserializeBundle(json);
    	log.debug("FHIR bundle: {}", json);
    	return create(bundle);
    }

	@Override
    public boolean delete(String identifier) {
    	DocumentReference documentReference = fhirClient.getDocumentReference(identifier);
    	Composition composition = fhirClient.getComposition(documentReference);
    	Bundle document = fhirClient.getDocument(composition);
//    	ResourceRelationshipUtility.run(document);
    	List<IdType> idTypes = getIdTypesToDelete(document);
    	if (idTypes.isEmpty()) return false;
    	return delete(idTypes);
    }

	@Override
    public boolean replace(FhirPublicationDTO body) {
		String identifier = body.getIdentifier();
		Bundle bundle = deserializeBundle(body.getJsonString());
		String previousId = getDocumentReferenceId(identifier);
    	if (previousId == null) throw new BusinessException("DocumentReference not found");
    	setRelatedDocumentReference(bundle, previousId);
    	delete(identifier);
    	create(bundle);
    	return true;
    }

	@Override
    public boolean updateMetadata(FhirPublicationDTO body) {
    	String identifier = body.getIdentifier();
    	DocumentReference documentReference = fhirClient.getDocumentReference(identifier);
    	setMetadata(documentReference, body.getJsonString());
    	boolean result = fhirClient.update(documentReference);
    	return result;
    }

    @Override
    public boolean checkExists(final String masterIdentifier) {
        boolean isFound = fhirClient.checkExists(masterIdentifier);
        log.info("found?: {}", isFound);
        return isFound;
    }
    
	@Override
    public String translateCode(String code, String system, String targetSystem) {
        String out = fhirClient.translateCode(code, system, targetSystem);
        log.info("Code translated result: {}", out);
        return out;
    }

	private Bundle deserializeBundle(String json) {
		return FHIRR4Helper.deserializeResource(Bundle.class, json, true);		
	}
	
    private boolean create(Bundle bundle) {
    	Bundle newBundle = fhirClient.create(bundle);
    	return newBundle != null && newBundle.hasEntry();
	}

    private String getDocumentReferenceId(String masterIdentifier) {
    	DocumentReference documentReference = fhirClient.getDocumentReference(masterIdentifier);
    	if (documentReference == null) return null;
    	return documentReference.getId();
	}

    private void setMetadata(DocumentReference documentReference, String jsonString) {
    	// TODO 
    	documentReference.getCategory().get(0).getCoding().get(0).setCode("livBasso");
	}

    private void setRelatedDocumentReference(Bundle bundle, String previousIdentifier) {
    	bundle
		.getEntry()
		.stream()
		.filter(entry -> (entry.getResource() instanceof DocumentReference))
		.forEach(entry -> setRelatedDocumentReference(entry.getResource(), previousIdentifier));
	}

	private void setRelatedDocumentReference(Resource documentReference, String previousIdentifier) {
		DocumentReference reference = (DocumentReference) documentReference;
		if (reference.getRelatesTo() == null) reference.setRelatesTo(new ArrayList<>());
		DocumentReferenceRelatesToComponent related = getRelatedDocumentReference(previousIdentifier);
		reference.getRelatesTo().add(related);
	}

	private DocumentReferenceRelatesToComponent getRelatedDocumentReference(String previousIdentifier) {
		DocumentReferenceRelatesToComponent related = new DocumentReferenceRelatesToComponent();
		DocumentRelationshipType code = DocumentRelationshipType.REPLACES;
		related.setCode(code);
		related.setId(previousIdentifier);
		return related;
	}

	private boolean delete(List<IdType> idTypes) {
		return idTypes
				.stream()
				.map(idType -> fhirClient.deleteResource(idType))
				.allMatch(result -> result);
	}

	private List<IdType> getIdTypesToDelete(Bundle document) {
		if (!document.hasEntry()) return new ArrayList<>();
		return document
				.getEntry()
				.stream()
				.map(entry -> entry.getResource())
				.filter(this::canBeDeleted)
				.map(Resource::getIdElement)
				.collect(Collectors.toList());
	}
	
	private boolean canBeDeleted(Resource resource1) {
		return true;
	}
	
}
