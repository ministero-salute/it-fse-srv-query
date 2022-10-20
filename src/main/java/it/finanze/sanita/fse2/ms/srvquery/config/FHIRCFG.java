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
public class FHIRCFG implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7544807186612242808L;

	// /** EDS Ingestion Config **/
	// @Value("${eds-ingestion.url.host}")
	// private String edsIngestionHost;

	// @Value("${eds-ingestion.url.path.create}")
	// private String edsIngestionCreatePath;

	// @Value("${eds-ingestion.url.path.replace}")
	// private String edsIngestionReplacePath;
	
	// @Value("${eds-ingestion.url.path.update}")
	// private String edsIngestionUpdatePath; 
	
	// @Value("${eds-ingestion.url.path.delete}")
	// private String edsIngestionDeletePath;
	
	/** 
	 * FHIR Server Test URL 
	 */
	@Value("${fhir-server-test-url}")
	private String fhirServerTestUrl;

}
