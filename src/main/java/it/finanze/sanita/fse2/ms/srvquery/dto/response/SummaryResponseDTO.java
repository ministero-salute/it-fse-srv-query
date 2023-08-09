package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SummaryResponseDTO extends ResponseDTO {
	
	private List<SummaryResourceDTO> summaryDetailDTO;


	public SummaryResponseDTO(LogTraceInfoDTO traceInfo, List<SummaryResourceDTO> summaryDetailDTO) {
		super(traceInfo);
		this.summaryDetailDTO = summaryDetailDTO;
	}

}