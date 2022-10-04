package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Riccardo Bonesi
 *
 *	DTO used to return check exist result.
 */
@Getter
@Setter
public class ResourceExistResDTO extends ResponseDTO {


    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550025571939901939L;
	
	/** 
	 * True if document already exists 
	 */
	private boolean exist;
	
	public ResourceExistResDTO() {
		super();
		exist = false;
	}

	public ResourceExistResDTO(final LogTraceInfoDTO traceInfo, final boolean inExist) {
		super(traceInfo);
		exist = inExist;
	}
	
    
}
