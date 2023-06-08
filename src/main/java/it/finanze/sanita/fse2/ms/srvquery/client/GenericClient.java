/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericClient {

	protected void handleErrors(HttpClientErrorException httpError) {
		
		if(HttpStatus.NOT_FOUND.equals(httpError.getStatusCode())) {
			log.error(httpError.getMessage());
		}
	}
}
