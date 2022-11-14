/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service;

import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;

/** 
 * Interface for creation of document in the FHIR Server 
 *
 */
public interface IFHIRSRV {

    /**
     * Create a new FHIR resource in the FHIR server
     * @param createDTO  The DTO to create 
     * @return Boolean  The result of the creation 
     */
	Boolean create(FhirPublicationDTO createDTO);

    
    /**
     * Check if a document reference exist on FHIR server
     * 
     * @param masterIdentifier  The master identifier of the document to search 
     * @return boolean  True if the document exists on ElasticSearch 
     */
    boolean checkExist(String masterIdentifier);
    
    void delete(String masterIdentifier);
}
