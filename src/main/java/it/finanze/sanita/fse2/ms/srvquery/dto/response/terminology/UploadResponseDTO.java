package it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UploadResponseDTO extends ResponseDTO {

	private String location;
	
	private Integer insertedItems;
	
	public UploadResponseDTO(final LogTraceInfoDTO traceInfo,final String inLocation, final Integer inInsertedItems) {
		super(traceInfo);
		location = inLocation;
		insertedItems = inInsertedItems;
	}
	
}
