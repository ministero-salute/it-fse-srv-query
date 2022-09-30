package it.finanze.sanita.fse2.ms.srvquery.exceptions;

public class ElementNotFoundException extends Exception {


	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 182266721949057193L; 
	

	/**
     * Complete constructor.
     *
     * @param msg	Message to be shown.
     *              It should describe what the operation was trying to accomplish.
     * @param e		The original MongoExceptions.
     */
    public ElementNotFoundException(final String msg) {
        super(msg);
        
    }
}
