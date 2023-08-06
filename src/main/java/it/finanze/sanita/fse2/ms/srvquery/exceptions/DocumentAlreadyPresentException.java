/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.exceptions;

import static it.finanze.sanita.fse2.ms.srvquery.enums.ErrorClassEnum.CONFLICT;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.base.ConflictException;

public class DocumentAlreadyPresentException extends ConflictException {
	 
    /**
	 * Serial verison uid.
	 */
	private static final long serialVersionUID = 5238920610659124236L;

	public DocumentAlreadyPresentException(String msg) {
        super(new ErrorDTO(CONFLICT.getType(), CONFLICT.getTitle(), msg, CONFLICT.getInstance()));
    }
    
}
