/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * The Class TerminologyResponseDTO.
 * 	Terminology Response.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DelDocsResDTO extends ResponseDTO {

	public DelDocsResDTO(final LogTraceInfoDTO traceInfo) {
		super(traceInfo);
	}
	
}
