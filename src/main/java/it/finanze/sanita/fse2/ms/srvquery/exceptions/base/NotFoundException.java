/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.exceptions.base;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotFoundException extends RuntimeException {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5129068145500002117L;
	
	private final transient ErrorDTO error;

}
