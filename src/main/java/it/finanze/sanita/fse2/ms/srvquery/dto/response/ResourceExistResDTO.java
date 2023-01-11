/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 *
 *	DTO used to return check exist result.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResourceExistResDTO extends ResponseDTO {

	/** 
	 * True if document already exists 
	 */
	private boolean exist;
	
	public ResourceExistResDTO(final LogTraceInfoDTO traceInfo, final boolean inExist) {
		super(traceInfo);
		exist = inExist;
	}
	
    
}
