/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IDocumentCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DeleteResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ReplaceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResourceExistResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.UpdateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.UnknownException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import lombok.extern.slf4j.Slf4j;

/** 
 * The document CTL Implementation 
 * 
 */
@RestController
@Slf4j
public class DocumentCTL extends AbstractCTL implements IDocumentCTL {

	 
	/** 
	 * The FHIR Service 
	 */
    @Autowired
    private IFHIRSRV fhirSRV;
    
    
    @Override
    public CreateResponseDTO create(FhirPublicationDTO body,final HttpServletRequest request){
    	log.info("[FHIR] Create - START");
    	CreateResponseDTO output = new CreateResponseDTO();
    	try {
    		boolean result = fhirSRV.create(body);
    		output.setEsito(result);
    	} catch (UnknownException e) {
    		output.setEsito(false);
    		output.setMessage("Eccezione di test");
		} catch(Exception ex) {
    		output.setEsito(false);
    		output.setMessage(ex.getMessage());
    	}
    	return output;
    }

    @Override
    public DeleteResponseDTO delete(final String identifier,final HttpServletRequest request) {
        log.info("[FHIR] Delete - START");
        DeleteResponseDTO output = new DeleteResponseDTO();
        try {
        	boolean result = fhirSRV.delete(identifier);
        	output.setEsito(result);
        } catch(Exception ex) {
        	output.setEsito(false);
    		output.setMessage(ex.getMessage());
        }
        return output; 
    }

    @Override
    public ReplaceResponseDTO replace(FhirPublicationDTO body,final HttpServletRequest request) {
    	log.info("[FHIR] Replace - START");
    	ReplaceResponseDTO output = new ReplaceResponseDTO();
    	try {
    		boolean result = fhirSRV.replace(body);
    		output.setEsito(result);
    	} catch(Exception ex) {
    		output.setEsito(false);
    		output.setMessage(ex.getMessage());
    	}

    	return output;
    }
    
    @Override
    public UpdateResponseDTO updateMetadata(FhirPublicationDTO body,HttpServletRequest request) {
        log.info("[FHIR] Update - START");
        UpdateResponseDTO output = new UpdateResponseDTO();
    	try {
    		boolean result = fhirSRV.updateMetadata(body);
    		output.setEsito(result);
    	} catch(Exception ex) {
    		output.setEsito(false);
    		output.setMessage(ex.getMessage());
    	}

    	return output;
    }
    
    @Override
    public ResourceExistResDTO exist(final String id, final HttpServletRequest request) {
        log.debug("[FHIR] Check exist - START");
        boolean result = fhirSRV.checkExists(id);
        return new ResourceExistResDTO(getLogTraceInfo(), result);
    }
    
}
