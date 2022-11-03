/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FHIRCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/** 
 * FHIR Service Implementation 
 *
 */
@Service
@Slf4j
public class FHIRSRV implements IFHIRSRV {
	
	/** 
	 * Profile Utility 
	 */
    @Autowired
	private ProfileUtility profileUtility;

    /** 
     * FHIR Configuration 
     */
    @Autowired
	private FHIRCFG fhirCFG;

    /** 
     * FHIR Client 
     */
    private FHIRClient client;

    @PostConstruct
    void init() {
        client = new FHIRClient(fhirCFG.getFhirServerTestUrl());
    }

    @Override
    public Boolean create(final FhirPublicationDTO createDTO) {
    	boolean out = false;
    	try {
    		String json = createDTO.getJsonString();
    		Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, json, true);

    		client.saveBundleWithTransaction(bundle);
    		out = true;
    		log.debug("FHIR bundle: {}", json);
    	} catch(Exception e) {
    		log.error("Error creating new resource on FHIR Server: ", e);
    		throw new BusinessException(e);
    	}
    	return out;
    }

    @Override
    public String translateCode(String code, String system, String targetSystem) {
        String out = "";
		try {
            if (profileUtility.isDevProfile()) {
                out = client.translateCode(code, system, targetSystem);
                log.info("Code translated result: {}", out);
            } else {
                // TODO
            }

		} catch(Exception e) {
			log.error("Error translating Code from FHIR Terminology Server: ", e);
			throw new BusinessException(e);
		}
		return out;
    }

    @Override
    public boolean checkExist(final String masterIdentifier) {
        boolean isFound = client.read(masterIdentifier);
        log.info("found?: {}", isFound);
        return isFound;
    }
}
