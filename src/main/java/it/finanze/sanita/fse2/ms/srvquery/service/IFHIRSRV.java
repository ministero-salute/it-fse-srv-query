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
package it.finanze.sanita.fse2.ms.srvquery.service;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.ResourceSearchParameterDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;

/** 
 * Interface for creation of document in the FHIR Server 
 *
 */
public interface IFHIRSRV {

    /**
     * Create a new FHIR resource in the FHIR server
     * @param createDTO  The DTO to create 
     * @return boolean  The result of the creation 
     */
	boolean create(FhirPublicationDTO createDTO);

    /**
     * Delete a FHIR resource in the FHIR server
     * @param identifier  The masterIdentifier of the document 
     * @return boolean  The result of the delete
     */
	boolean delete(String identifier);
    
	/**
     * Replace a FHIR resource in the FHIR server
     * @param replaceDTO  The DTO to replace 
     * @return boolean  The result of the replace 
     */
	boolean replace(FhirPublicationDTO replaceDTO);

    /**
     * Update a FHIR resource metadata in the FHIR server
     * @param updateDTO  The DTO to update
     * @return boolean  The result of the update  
     */
	boolean updateMetadata(FhirPublicationDTO updateDTO);
 

    /**
     * Check if a document reference exist on FHIR server
     * 
     * @param masterIdentifier  The master identifier of the document to search 
     * @return boolean  True if the document exists on ElasticSearch 
     */
    boolean checkExists(String masterIdentifier);

    
    /**
     * Retrieve search parameters for all resources managed by the FHIR server
     * 
     * @return List<ResourceSearchParameterDTO> the parameters list
     */
    List<ResourceSearchParameterDTO> getResourcesSearchParameters();
    
}
