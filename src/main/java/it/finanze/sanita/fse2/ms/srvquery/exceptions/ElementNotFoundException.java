package it.finanze.sanita.fse2.ms.srvquery.exceptions;

/** 
 * Element not found exception 
 *
 */
public class ElementNotFoundException extends Exception {


	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 182266721949057193L; 
	


	/**
	 * Element Not Found Exception 
	 * 
	 * @param msg  The exception message 
	 */
    public ElementNotFoundException(final String msg) {
        super(msg);
        
    }
}
