/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Riccardo Bonesi
 *
 *	DTO used to return check exist result.
 */
@Getter
@Setter
public class DocumentReferenceResDTO extends ResponseDTO {


	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1556625571939901939L;
	
	/** 
	 * Document ID 
	 */
	private String id;

	/** 
	 * Document JSON String 
	 */
	private String jsonString;
	
	public DocumentReferenceResDTO() {
		super();
		id = null;
		jsonString = null;
	}

	public DocumentReferenceResDTO(final LogTraceInfoDTO traceInfo, final String inId, final String inJsonString) {
		super(traceInfo);
		id = inId;
		jsonString = inJsonString;
	}
}