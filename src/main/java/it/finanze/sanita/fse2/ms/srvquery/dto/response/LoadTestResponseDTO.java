package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.LoadResultDTO;
import lombok.Data;


@Data
public class LoadTestResponseDTO extends ResponseDTO {

	private List<LoadResultDTO> result;
	
	public LoadTestResponseDTO(final LogTraceInfoDTO traceInfo, final List<LoadResultDTO> inResult) {
		super(traceInfo);
		result = inResult;
	}
	
}
