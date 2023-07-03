package it.finanze.sanita.fse2.ms.srvquery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ClientException;

class ClientExceptionTest {

	@Test
    void testClientException() {
        ErrorDTO error = new ErrorDTO();
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(new LogTraceInfoDTO(null, null), error);
        Integer statusCode = 404;

        ClientException clientException = new ClientException(errorResponse, statusCode);

        Assertions.assertEquals(errorResponse, clientException.getError());
        Assertions.assertEquals(statusCode, clientException.getStatusCode());
    }
	
}
