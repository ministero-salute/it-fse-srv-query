package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import it.finanze.sanita.fse2.ms.srvquery.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 
 * Log Trace Info DTO 
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogTraceInfoDTO extends AbstractDTO {

	/**
	 * Span ID.
	 */
	private final String spanID;
	
	/**
	 * Trace ID.
	 */
	private final String traceID;

}
