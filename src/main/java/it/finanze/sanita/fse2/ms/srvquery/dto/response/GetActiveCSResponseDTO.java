/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.GetActiveResourceDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *	DTO used to return check exist result.
 */
@Getter
@Setter
@NoArgsConstructor
public class GetActiveCSResponseDTO extends ResponseDTO {

	private List<GetActiveResourceDTO> activeResources;
	
	public GetActiveCSResponseDTO(final LogTraceInfoDTO traceInfo, final List<GetActiveResourceDTO> inActiveResources) {
		super(traceInfo);
		activeResources = inActiveResources;
	}
	
    
}
