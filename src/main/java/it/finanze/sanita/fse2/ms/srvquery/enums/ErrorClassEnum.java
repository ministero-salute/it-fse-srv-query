package it.finanze.sanita.fse2.ms.srvquery.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorClassEnum {

	/**
	 * Generic class error.
	 */
	GENERIC("/errors", "Generic", "Errore generico", "/generic"),
	VALIDATION_MANDATORY("/errors/fields", "Missing", "Campo obbligatorio non presente", "/mandatory"),
    TIMEOUT("/errors", "Timeout", "Errore di timeout", "/timeout");


	/**
	 * Error type.
	 */
	private final String type;

	/**
	 * Error title, user friendly description.
	 */
	private final String title;

	/**
	 * Error detail, developer friendly description.
	 */
	private final String detail;

	/**
	 * Error instance, URI that identifies the specific occurrence of the problem.
	 */
	private final String instance;

}