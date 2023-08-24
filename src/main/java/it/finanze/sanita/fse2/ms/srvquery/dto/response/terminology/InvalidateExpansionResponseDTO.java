package it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.InvalidateResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class InvalidateExpansionResponseDTO extends ResponseDTO {

	private List<InvalidateResultDTO> info;
	
	public InvalidateExpansionResponseDTO(final LogTraceInfoDTO traceInfo, final List<InvalidateResultDTO> inInfo) {
		super(traceInfo);
		info = inInfo;
	}
	
}
