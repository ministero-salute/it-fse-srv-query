package it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import lombok.Data;


@Data
public class GetResponseDTO extends ResponseDTO {

	private boolean isPresent;
	
	public GetResponseDTO(final LogTraceInfoDTO traceInfo, final boolean inIsPresent) {
		super(traceInfo);
		isPresent = inIsPresent;
	}
	
}
