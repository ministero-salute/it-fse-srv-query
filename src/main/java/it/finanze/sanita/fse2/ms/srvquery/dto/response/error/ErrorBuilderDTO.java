/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response.error;


import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.ErrorInstance.Server;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

/** 
 * Builds the error responses according to the given exception 
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorBuilderDTO {

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
}
