/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response.error;


import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorInstance.Resource;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorInstance.Server;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceAlreadyPresentException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;

import static org.apache.http.HttpStatus.*;

/** 
 * Builds the error responses according to the given exception 
 *
 */
public final class ErrorBuilderDTO {

    /**
     * Private constructor to disallow to access from other classes
     */
    private ErrorBuilderDTO() {}



    /**
     * Builds a Generic Exception 
     * 
     * @param trace  The LogInfoDTO with trace and span ID 
     * @param ex  The Exception 
     * @return ErrorResponseDTO  The error response 
     */
    public static ErrorResponseDTO createGenericError(LogTraceInfoDTO trace, Exception ex) {
        return new ErrorResponseDTO(
            trace,
            ErrorType.SERVER.getType(),
            ErrorType.SERVER.getTitle(),
            ex.getMessage(),
            SC_INTERNAL_SERVER_ERROR,
            ErrorType.SERVER.toInstance(Server.INTERNAL)
        );
    }

 
    /**
     * Builds a Document Not Found Exception 
     * 
     * @param trace  The LogInfoDTO with trace and span ID 
     * @param ex  The Exception 
     * @return ErrorResponseDTO  The error response 
     */
    public static ErrorResponseDTO createDocumentNotFoundError(LogTraceInfoDTO trace, ResourceNotFoundException ex) {
        return new ErrorResponseDTO(
            trace,
            ErrorType.RESOURCE.getType(),
            ErrorType.RESOURCE.getTitle(),
            ex.getMessage(),
            SC_NOT_FOUND,
            ErrorType.RESOURCE.toInstance(Resource.NOT_FOUND)
        );
    }

    /**
     * Builds a Document Already Present Exception 
     * 
     * @param trace  The LogInfoDTO with trace and span ID 
     * @param ex  The Exception 
     * @return ErrorResponseDTO  The error response 
     */
    public static ErrorResponseDTO createDocumentAlreadyPresentError(LogTraceInfoDTO trace, ResourceAlreadyPresentException ex) {
        return new ErrorResponseDTO(
            trace,
            ErrorType.RESOURCE.getType(),
            ErrorType.RESOURCE.getTitle(),
            ex.getMessage(),
            SC_CONFLICT,
            ErrorType.RESOURCE.toInstance(Resource.CONFLICT)
        );
    } 
   



}
