package it.finanze.sanita.fse2.ms.srvquery.controller.handler;




import static it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorBuilderDTO.createDocumentAlreadyPresentError;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorBuilderDTO.createDocumentNotFoundError;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorBuilderDTO.createGenericError;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorBuilderDTO.createOperationError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.OperationException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceAlreadyPresentException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 *	Exceptions handler
 *  @author Riccardo Bonesi
 */
@ControllerAdvice
@Slf4j
public class ExceptionCTL extends ResponseEntityExceptionHandler {

    /**
     * Tracker log.
     */
    @Autowired
    private Tracer tracer;


    /**
     * Handle resource not found exception.
     *
     * @param ex		exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // Log me
        log.warn("HANDLER handleResourceNotFoundException()");
        log.error("HANDLER handleResourceNotFoundException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createDocumentNotFoundError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        // Bye bye
        return new ResponseEntity<>(out, headers, out.getStatus());
    }
    
    /**
     * Handle resource already present exception.
     *
     * @param ex		exception
     */
    @ExceptionHandler(ResourceAlreadyPresentException.class)
    protected ResponseEntity<ErrorResponseDTO> handleResourceAlreadyPresentException(ResourceAlreadyPresentException ex) {
        // Log me
        log.warn("HANDLER handleResourceAlreadyPresentException()");
        log.error("HANDLER handleResourceAlreadyPresentException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createDocumentAlreadyPresentError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        // Bye bye
        return new ResponseEntity<>(out, headers, out.getStatus());
    } 
    


    /**
     * Handle operation exception.
     *
     * @param ex		exception
     */
    @ExceptionHandler(OperationException.class)
    protected ResponseEntity<ErrorResponseDTO> handleOperationException(OperationException ex) {
        // Log me
        log.warn("HANDLER handleOperationException()");
        log.error("HANDLER handleOperationException()", ex);
        // Create error DTO
        ErrorResponseDTO out = createOperationError(getLogTraceInfo(), ex);
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        // Bye bye
        return new ResponseEntity<>(out, headers, out.getStatus());
    }





    /**
     * Handle generic exception.
     *
     * @param ex		exception
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
