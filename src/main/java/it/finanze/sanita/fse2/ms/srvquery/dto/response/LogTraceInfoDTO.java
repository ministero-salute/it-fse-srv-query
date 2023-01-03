/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Data;

/** 
 * Log Trace Info DTO 
 *
 */
@Data
public class LogTraceInfoDTO {

	/**
	 * Span ID.
	 */
	private final String spanID;
	
	/**
	 * Trace ID.
	 */
	private final String traceID;

}
