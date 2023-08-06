/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import org.hl7.fhir.r4.model.MetadataResource;

import lombok.Data;

@Data
public class TranslateResponseDTO extends ResponseDTO {

	private boolean esito;
	private String message;
	private MetadataResource mr;
	
	public TranslateResponseDTO(LogTraceInfoDTO traceInfo, Boolean inEsito, String inMessage, MetadataResource inMR) {
		super(traceInfo);
		esito = inEsito;
		message = inMessage;
		mr = inMR;
	}
}
