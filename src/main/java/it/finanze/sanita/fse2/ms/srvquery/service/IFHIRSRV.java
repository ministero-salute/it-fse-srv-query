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
     * Translate a code into a different codeSystem 
     * 
     * @param code  The code to be translated 
     * @param system  The starting system 
     * @param targetSystem  The target system 
     * @return String  The translated code 
     */
    String translateCode(String code, String system, String targetSystem);

    /**
     * Check if a document reference exist on FHIR server
     * 
     * @param masterIdentifier  The master identifier of the document to search 
     * @return boolean  True if the document exists on ElasticSearch 
     */
    boolean checkExist(String masterIdentifier);
}
