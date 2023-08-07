package it.finanze.sanita.fse2.ms.srvquery.exceptions;

import static it.finanze.sanita.fse2.ms.srvquery.enums.ErrorClassEnum.METADATARESOURCE_NOTFOUND;

import it.finanze.sanita.fse2.ms.srvquery.dto.ErrorDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.base.NotFoundException;

public class MetadataResourceNotFoundException  extends NotFoundException {
	 
    /**
	 * Serial verison uid.
	 */
	private static final long serialVersionUID = 5238920610659124236L;

	public MetadataResourceNotFoundException(String msg) {
        super(new ErrorDTO(METADATARESOURCE_NOTFOUND.getType(), METADATARESOURCE_NOTFOUND.getTitle(), msg, METADATARESOURCE_NOTFOUND.getInstance()));
    }
    
}
