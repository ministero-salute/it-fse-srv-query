package it.finanze.sanita.fse2.ms.srvquery.exceptions;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.base.NotFoundException;

public class CodeSystemNotFoundException extends NotFoundException {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 6437220661832499496L;

	public CodeSystemNotFoundException(final String msg) {
		super(new ErrorDTO("type", "title", msg, "instance"));
	}

}
