/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility.deserializeBundle;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility;
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
    	return fhirClient.create(bundle);
    }

	@Override
    public boolean delete(String identifier) {
    	DocumentReference documentReference = fhirClient.getDocumentReference(identifier);
    	Composition composition = fhirClient.getComposition(documentReference);
    	Bundle bundleToDelete = fhirClient.getDocument(composition);
    	FHIRUtility.prepareForDelete(bundleToDelete, documentReference);
    	return fhirClient.delete(bundleToDelete);
    }

	@Override
    public boolean replace(FhirPublicationDTO body) {
		Bundle bundleToReplace = deserializeBundle(body.getJsonString());
		String identifier = body.getIdentifier();
		DocumentReference documentReference = fhirClient.getDocumentReference(identifier);
    	Composition composition = fhirClient.getComposition(documentReference);
    	Bundle document = fhirClient.getDocument(composition);
    	FHIRUtility.prepareForReplace(bundleToReplace, documentReference, document);
    	return fhirClient.replace(bundleToReplace);
    }

	@Override
    public boolean updateMetadata(FhirPublicationDTO body) {
    	String identifier = body.getIdentifier();
    	DocumentReference documentReference = fhirClient.getDocumentReference(identifier);
    	FHIRUtility.prepareForUpdate(documentReference, body.getJsonString());
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
    
}
