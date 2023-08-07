package it.finanze.sanita.fse2.ms.srvquery.exceptions;

import static it.finanze.sanita.fse2.ms.srvquery.enums.ErrorClassEnum.VALIDATION_VERSION;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.base.ValidationException;

public class DiffCheckerFirstVersionException extends ValidationException {
	 
    /**
	 * Serial verison uid.
	 */
	private static final long serialVersionUID = 5238920610659124236L;

	public DiffCheckerFirstVersionException(String msg) {
        super(new ErrorDTO(VALIDATION_VERSION.getType(), VALIDATION_VERSION.getTitle(), msg, VALIDATION_VERSION.getInstance()));
    }
    
}
