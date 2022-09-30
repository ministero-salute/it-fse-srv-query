package it.finanze.sanita.fse2.ms.srvquery.service;

import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;

public interface IFHIRSRV {

    /**
     * Create e new FHIR resource in the FHIR server
     * @param createDTO
     * @return
     */
	Boolean create(FhirPublicationDTO createDTO);

    /**
     * Translate a code into a different codeSystem
     * @param code
     * @param system
     * @param targetSystem
     * @return
     */
    String translateCode(String code, String system, String targetSystem);

    /**
     * Check if a document reference exist on FHIR server
     * @param masterIdentifier
     * @return
     */
    boolean checkExist(String masterIdentifier);
}
