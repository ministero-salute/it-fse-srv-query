package it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class GetResponseDTO extends ResponseDTO {

	private boolean isPresent;
	
	private String id;
	
	public GetResponseDTO(final LogTraceInfoDTO traceInfo, final boolean inIsPresent, final String inId) {
		super(traceInfo);
		isPresent = inIsPresent;
		id = inId;
	}
	
}
