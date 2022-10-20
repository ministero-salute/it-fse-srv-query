/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.exceptions;

/** 
 * Kafka Exception 
 *
 */
public class KafkaException extends Exception {

	

	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = -2915112012591501283L; 
	
	

	/**
     * Complete constructor.
     *
     * @param msg	Message to be shown.
     *              It should describe what the operation was trying to accomplish.
     * @param e		The original MongoExceptions.
     */
    public KafkaException(final String msg, final Exception e) {
        super(msg, e);
        
    }
    
}
