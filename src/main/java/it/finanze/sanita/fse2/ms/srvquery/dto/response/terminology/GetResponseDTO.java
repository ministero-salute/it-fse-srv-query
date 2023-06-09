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
	private Integer counter;
	
	public GetResponseDTO(final LogTraceInfoDTO traceInfo, final boolean inIsPresent, final String inId, final Integer inCounter) {
		super(traceInfo);
		isPresent = inIsPresent;
		id = inId;
		counter = inCounter;
	}
	
}
