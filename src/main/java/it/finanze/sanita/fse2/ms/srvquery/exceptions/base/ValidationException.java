/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.exceptions.base;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationException extends RuntimeException {

    /**
	 * Serial version uuid.
	 */
	private static final long serialVersionUID = 80950179850288286L;
	
	private final transient ErrorDTO error;
}
