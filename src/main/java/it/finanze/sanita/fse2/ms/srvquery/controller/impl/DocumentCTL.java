/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IDocumentCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.*;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
    	final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
    	
    	log.info(Constants.Logs.START_LOG, Constants.Logs.CREATE,
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);
    	
    	CreateResponseDTO output = new CreateResponseDTO();
    	try {
    		boolean result = fhirSRV.create(body);
    		output.setEsito(result);
    	} catch(Exception ex) {
    		output.setEsito(false);
    		output.setMessage(ex.getMessage());
    	}
    	
    	log.info(Constants.Logs.EXIT_LOG, Constants.Logs.CREATE,
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);
    	
    	return output;
    }

    @Override
    public DeleteResponseDTO delete(final String identifier,final HttpServletRequest request) {
    	final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
    	
    	log.info("[START] {}() with arguments {}={}, {}={}", "delete",
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID(),
    			"identifier", identifier
    			);
    	
        DeleteResponseDTO output = new DeleteResponseDTO();
        try {
        	boolean result = fhirSRV.delete(identifier);
        	output.setEsito(result);
        } catch(Exception ex) {
        	output.setEsito(false);
    		output.setMessage(ex.getMessage());
        }
        
        log.info("[EXIT] {}() with arguments {}={}, {}={}", "delete",
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID(),
    			"identifier", identifier
    			);
        
        return output; 
    }

    @Override
    public ReplaceResponseDTO replace(FhirPublicationDTO body,final HttpServletRequest request) {
    	final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
    	
    	log.info(Constants.Logs.START_LOG, "replace",
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);
    	
    	ReplaceResponseDTO output = new ReplaceResponseDTO();
    	try {
    		boolean result = fhirSRV.replace(body);
    		output.setEsito(result);
    	} catch(Exception ex) {
    		output.setEsito(false);
    		output.setMessage(ex.getMessage());
    	}
    	
    	log.info(Constants.Logs.EXIT_LOG, "replace",
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);

    	return output;
    }
    
    @Override
    public UpdateResponseDTO updateMetadata(FhirPublicationDTO body,HttpServletRequest request) {
    	final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
    	
    	log.info(Constants.Logs.START_LOG, Constants.Logs.UPDATE,
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);
    	
        UpdateResponseDTO output = new UpdateResponseDTO();
    	try {
    		boolean result = fhirSRV.updateMetadata(body);
    		output.setEsito(result);
    	} catch(Exception ex) {
    		output.setEsito(false);
    		output.setMessage(ex.getMessage());
    	}
    	
    	log.info(Constants.Logs.EXIT_LOG, Constants.Logs.UPDATE,
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);

    	return output;
    }
    
    @Override
    public ResourceExistResDTO exist(final String id, final HttpServletRequest request) {
    	final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
    	
    	log.info(Constants.Logs.START_LOG, Constants.Logs.EXIST,
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);
    	
        boolean result = fhirSRV.checkExists(id);
        
        log.info(Constants.Logs.EXIT_LOG, Constants.Logs.EXIST,
    			Constants.Logs.TRACE_ID, traceInfoDTO.getTraceID()
    			);
        
        return new ResourceExistResDTO(getLogTraceInfo(), result);
    }
    
}
