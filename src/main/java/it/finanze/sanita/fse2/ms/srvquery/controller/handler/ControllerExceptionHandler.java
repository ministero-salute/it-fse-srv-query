/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller.handler;


import static it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorBuilderDTO.createGenericError;
import static it.finanze.sanita.fse2.ms.srvquery.enums.ErrorClassEnum.TIMEOUT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ClientException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.DocumentAlreadyPresentException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.base.ConflictException;
import lombok.extern.slf4j.Slf4j;

/**
 *	Exceptions handler
 */
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Tracker log.
     */
    @Autowired
    private Tracer tracer;
    

    /**
     * Handles Generic Exception 
     * 
     * @param ex  Exception 
     * @return ErrorResponseDTO  A DTO representing the error response 
     */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        // Log me
        log.warn("HANDLER handleGenericException()");
        log.error("HANDLER handleGenericException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createGenericError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        // Bye bye
        return new ResponseEntity<>(out, headers, out.getStatus());
    } 

    /**
   	 * Handles generic or unknown exceptions, unexpected thrown during the execution of any operation.
   	 *
   	 * @param ex exception
   	 */
   	@ExceptionHandler(value = {DocumentAlreadyPresentException.class})
   	protected ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex) {
   		log.error("HANDLER handleConflictException()", ex);
   		HttpHeaders headers = new HttpHeaders();
   		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
   		LogTraceInfoDTO traceInfo = getLogTraceInfo();
		ErrorResponseDTO response = new ErrorResponseDTO(traceInfo, ex.getError());

		return new ResponseEntity<>(response, headers, HttpStatus.CONFLICT);
   	}
    
    /**
	 * Handles generic or unknown exceptions, unexpected thrown during the execution of any operation.
	 *
	 * @param ex exception
	 */
	@ExceptionHandler(value = {ResourceAccessException.class})
	protected ResponseEntity<ErrorResponseDTO> handleResourceAccessException(ResourceAccessException ex) {
		log.error("HANDLER handleResourceAccessException()", ex);
		ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), TIMEOUT.getType(), TIMEOUT.getTitle(), TIMEOUT.getDetail(), 504, TIMEOUT.getInstance());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		return new ResponseEntity<>(out, headers, out.getStatus());
	}
	
	/**
	 * Handles generic or unknown exceptions, unexpected thrown during the execution of any operation.
	 *
	 * @param ex exception
	 */
	@ExceptionHandler(value = {ClientException.class})
	protected ResponseEntity<ErrorResponseDTO> handleResourceAccessException(ClientException ex) {
		log.error("HANDLER handleResourceAccessException()", ex);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		return new ResponseEntity<>(ex.getError(), headers, ex.getStatusCode());
	}
	
    
    
    /**
     * Generate a new {@link LogTraceInfoDTO} instance
     * @return The new instance
     */
    private LogTraceInfoDTO getLogTraceInfo() {
        // Create instance
        LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
        // Verify if context is available
        if (tracer.currentSpan() != null) {
            out = new LogTraceInfoDTO(
                tracer.currentSpan().context().spanIdString(),
                tracer.currentSpan().context().traceIdString());
        }
        // Return the log trace
        return out;
    }
}
