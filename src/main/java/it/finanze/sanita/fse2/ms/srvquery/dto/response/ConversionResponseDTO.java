package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConversionResponseDTO extends ResponseDTO {

	private String result;
	
	public ConversionResponseDTO(final LogTraceInfoDTO traceInfo, final String inResult) {
		super(traceInfo);
		result = inResult;
	}
}
