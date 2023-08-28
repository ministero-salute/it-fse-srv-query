package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.MetadataResource;

@Data
@NoArgsConstructor
public class SummaryResponseDTO extends ResponseDTO {
	
	private List<SummaryResourceDTO> summaryDetailDTO;


	public SummaryResponseDTO(LogTraceInfoDTO traceInfo, List<SummaryResourceDTO> summaryDetailDTO) {
		super(traceInfo);
		this.summaryDetailDTO = summaryDetailDTO;
	}

	public static SummaryResponseDTO fromResources(LogTraceInfoDTO info, List<MetadataResource> resources) {
		List<SummaryResourceDTO> res = resources.stream().map(SummaryResourceDTO::fromResource).collect(Collectors.toList());
		return new SummaryResponseDTO(info, res);
	}

}