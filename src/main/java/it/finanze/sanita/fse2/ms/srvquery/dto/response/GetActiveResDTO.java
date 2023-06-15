/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *	DTO used to return check exist result.
 */
@Getter
@Setter
@NoArgsConstructor
public class GetActiveResDTO extends ResponseDTO {

	private List<String> ids;
	
	public GetActiveResDTO(final LogTraceInfoDTO traceInfo, final List<String> inIds) {
		super(traceInfo);
		ids = inIds;
	}
	
    
}
