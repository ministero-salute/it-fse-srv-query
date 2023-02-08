/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.request;


import static it.finanze.sanita.fse2.ms.srvquery.utility.ValidationUtility.DEFAULT_STRING_MAX_SIZE;
import static it.finanze.sanita.fse2.ms.srvquery.utility.ValidationUtility.DEFAULT_STRING_MIN_SIZE;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 * DTO representing a FHIR Resource 
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FhirPublicationDTO {

	/** 
	 * Document identifier 
	 */
	@Size(min = DEFAULT_STRING_MIN_SIZE, max = DEFAULT_STRING_MAX_SIZE)
	private String identifier; 
	
	/** 
	 * Document Json String
	 */
	@Size(min = DEFAULT_STRING_MIN_SIZE, max = DEFAULT_STRING_MAX_SIZE)
	private String jsonString;
}
