package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IDocumentCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResourceExistResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.service.IKafkaSRV;
import lombok.extern.slf4j.Slf4j;

/** 
 * The document CTL Implementation 
 * 
 * @author Riccardo Bonesi
 */
@RestController
@Slf4j
public class DocumentCTL extends AbstractCTL implements IDocumentCTL {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 4761820033633287423L;

	/** 
	 * The FHIR Service 
	 */
    @Autowired
    private transient IFHIRSRV fhirSRV;

    /** 
     * The Kafka Service 
     */
    @Autowired
	private IKafkaSRV kafkaSRV;
    
    
    @Override
    public ResponseDTO create(final HttpServletRequest request, final FhirPublicationDTO body){
        log.info("[FHIR] Create - START");
        log.debug("Received: " + body.toString());

        // create resource on FHIR Server
        Boolean result = fhirSRV.create(body);

        // if the FHIR creation was successful create also on Elasticsearch
        if(result){
            kafkaSRV.sendCreateElasticsearch(body);
            
        } else {
            // TODO: tornare messaggio di errore;
        }

        return new ResponseDTO();
        
    }

    @Override
    public ResponseDTO delete(final HttpServletRequest request, final String identifier){
        log.info("[FHIR] Delete - START");
        log.debug("Deleting resource: " + identifier);
        // TODO
        // identifier (masteridentifier) della DocumentReference -> faccio una GET, mi ritorna la DocumentReference, estraggo la composition, faccio la delete delle risorse che voglio eliminare
        return new ResponseDTO(); 

    }

    @Override
    public ResourceExistResDTO exist(final String id, final HttpServletRequest request) {
        log.info("[FHIR] Check exist - START");
        // TODO check esistenza su Elasticsearch
        boolean result = fhirSRV.checkExist(id);
        return new ResourceExistResDTO(getLogTraceInfo(), result);
    }

    @Override
    public DocumentReferenceResDTO getDocumentById(final HttpServletRequest request, final String id) throws ResourceNotFoundException {
        log.info("[FHIR] Get document by Id - START");
        // TODO
        return new DocumentReferenceResDTO(getLogTraceInfo(), id, "string");
    }

    @Override
    public ResponseDTO replace(final HttpServletRequest request, final FhirPublicationDTO body) {
        log.info("[FHIR] Replace - START");
        log.debug("Received: " + body.toString());

        // TODO: delete e poi create
        
        return new ResponseDTO();
    }

    @Override
    public ResponseDTO updateMetadata(final HttpServletRequest request, final FhirPublicationDTO body) {
        log.info("[FHIR] Update - START");
        log.debug("Received: " + body.toString());
        // TODO
        return new ResponseDTO();
    }
    
}
