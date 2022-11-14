/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import javax.annotation.PostConstruct;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
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
	

    /** 
     * FHIR Configuration 
     */
    @Autowired
	private FhirCFG fhirCFG;

    /** 
     * FHIR Client 
     */
    private FHIRClient fhirClient;

    @PostConstruct
    void init() {
    	fhirClient = new FHIRClient(fhirCFG.getFhirServerUrl());
    }

    @Override
    public Boolean create(final FhirPublicationDTO createDTO) {
    	boolean out = false;
    	try {
    		String json = createDTO.getJsonString();
    		Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, json, true);
    		fhirClient.saveBundleWithTransaction(bundle);
    		out = true;
    		log.debug("FHIR bundle: {}", json);
    	} catch(Exception e) {
    		log.error("Error creating new resource on FHIR Server: ", e);
    		throw new BusinessException(e);
    	}
    	return out;
    }

    @Override
    public boolean checkExist(final String masterIdentifier) {
    	boolean exist = false;
    	try {
    		DocumentReference docReference = fhirClient.searchDocRefByMasterIdentifier(masterIdentifier);
    		if(docReference!=null) {
    			exist = true;
    		}
    	} catch(Exception ex) {
    		log.error("Error while perform check exist : " , ex);
    		throw new BusinessException("Error while perform check exist : " , ex);
    	}
        return exist;
    }
    
    
    @Override
    public void delete(final String masterIdentifier) {
    	boolean out = false;
    	try {
    		DocumentReference docReference = fhirClient.searchDocRefByMasterIdentifier(masterIdentifier);
    		if(docReference!=null && docReference.hasContext()) {
    			String compositionId = docReference.getContext().getRelatedFirstRep().getReference();
    			String url = fhirCFG.getFhirServerUrl() + "/" + compositionId;
    			Composition composition = fhirClient.searchCompositionByUrl(url);
    		} else {
    			//Set DTO for message
    		}
    	} catch(Exception e) {
    		log.error("Error creating new resource on FHIR Server: ", e);
    		throw new BusinessException(e);
    	}
    }
}
