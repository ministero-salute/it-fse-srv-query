/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO; 

/**
 * 
 *
 *	Abstract controller.
 */
public abstract class AbstractCTL implements Serializable {


	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = -7177691577253703309L; 
	
	
	@Autowired
	private transient Tracer tracer;


	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanIdString(), 
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}

}

