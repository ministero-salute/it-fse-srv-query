package it.finanze.sanita.fse2.ms.srvquery.exceptions;

public class UnknownException extends RuntimeException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 2940592148957799936L;

	
	/**
	 * Message constructor.
	 * 
	 * @param msg Message to be shown.
	 */
	public UnknownException(final String msg) {
		super(msg);
	}

	/**
	 * Complete constructor.
	 * 
	 * @param msg Message to be shown.
	 * @param e   Exception to be shown.
	 */
	public UnknownException(final String msg, final Exception e) {
		super(msg, e);
	}

	/**
	 * Exception constructor.
	 * 
	 * @param e Exception to be shown.
	 */
	public UnknownException(final Exception e) {
		super(e);
	}

}