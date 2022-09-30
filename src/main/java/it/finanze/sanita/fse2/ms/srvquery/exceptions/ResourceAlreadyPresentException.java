package it.finanze.sanita.fse2.ms.srvquery.exceptions;

public class ResourceAlreadyPresentException extends Exception {

    /**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 7925016596669482549L;

	
	/**
     * Message constructor.
     *
     * @param msg	Message to be shown.
     */
    public ResourceAlreadyPresentException(final String msg) {
        super(msg);
    }
}
