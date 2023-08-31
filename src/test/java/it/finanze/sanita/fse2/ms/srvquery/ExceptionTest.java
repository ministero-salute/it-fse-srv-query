/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ClientException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.DocumentAlreadyPresentException;
import org.springframework.http.HttpStatus;


class ExceptionTest {

	@Test
	void businessExceptionTest() {
		BusinessException exc = new BusinessException("Error"); 
		
		assertEquals(BusinessException.class, exc.getClass()); 
		assertEquals("Error", exc.getMessage()); 
		
	}
	
	@Test
	void businessExceptionTestWithoutMsg() {
		BusinessException exc = new BusinessException(new RuntimeException()); 
		
		assertEquals(BusinessException.class, exc.getClass()); 
		
	}

	@Test
	void documentAlreadyPresentExceptionTest() {
		DocumentAlreadyPresentException exc = new DocumentAlreadyPresentException("Error"); 
		
		assertEquals(DocumentAlreadyPresentException.class, exc.getClass()); 
		assertEquals("Error", exc.getError().getDetail()); 
		
	}

	@Test
	void testClientException() {
		ErrorDTO error = new ErrorDTO();
		ErrorResponseDTO errorResponse = new ErrorResponseDTO(new LogTraceInfoDTO(null, null), error, HttpStatus.NOT_FOUND.value());
		Integer statusCode = 404;

		ClientException clientException = new ClientException(errorResponse, statusCode);

		Assertions.assertEquals(errorResponse, clientException.getError());
		Assertions.assertEquals(statusCode, clientException.getStatusCode());
	}
	
} 
