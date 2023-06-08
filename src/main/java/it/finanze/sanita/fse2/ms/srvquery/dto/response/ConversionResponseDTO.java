package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversionResponseDTO extends ResponseDTO {

	private String message;
	
	public ConversionResponseDTO(final LogTraceInfoDTO traceInfo, final String inMessage) {
		super(traceInfo);
		message = inMessage;
	}
}
