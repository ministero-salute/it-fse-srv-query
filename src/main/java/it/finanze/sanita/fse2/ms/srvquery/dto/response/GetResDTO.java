package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *	DTO used to return check exist result.
 */
@Getter
@Setter
@NoArgsConstructor
public class GetResDTO extends ResponseDTO {

	private String oid;
	
	private byte[] content;
	
	public GetResDTO(final LogTraceInfoDTO traceInfo, final String inOid, final byte[] inContent) {
		super(traceInfo);
		oid = inOid;
		content = inContent;
	}
	
    
}
