/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCodeSystemResDTO extends ResponseDTO{
	
	private String id;
	
	
	public CreateCodeSystemResDTO(final LogTraceInfoDTO traceInfo, final String inID) {
		super(traceInfo);
		id = inID;
	}
	
}
