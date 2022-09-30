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
public class CodeTranslationResDTO extends ResponseDTO {


    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -1550025571939901939L;
	
	private String result;
	
	public CodeTranslationResDTO() {
		super();
		result = null;
	}

	public CodeTranslationResDTO(final LogTraceInfoDTO traceInfo, final String inResult) {
		super(traceInfo);
		result = inResult;
	}
	
    
}
