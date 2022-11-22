/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.config;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/** 
 * FHIR Server Config 
 *
 */
@Data
@Component
public class FhirCFG implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7544807186612242808L;
 
	/** 
	 * FHIR Server URL. 
	 */
	@Value("${fhir-server-url}")
	private String fhirServerUrl;
	
	/** 
	 * FHIR Server User. 
	 */
	@Value("${fhir-server-user}")
	private String fhirServerUser;
	
	/** 
	 * FHIR Server Pwd. 
	 */
	@Value("${fhir-server-pwd}")
	private String fhirServerPwd;

}
