package it.finanze.sanita.fse2.ms.srvquery.dto;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchResultDTO extends ResponseDTO {
	
	private String identifier;

	private List<ResourceDTO> metadataResourceDTO;

	public SearchResultDTO(LogTraceInfoDTO traceInfo, List<ResourceDTO> inMetadataResourceDTO) {
		super(traceInfo);
		metadataResourceDTO = inMetadataResourceDTO;
	}

}

