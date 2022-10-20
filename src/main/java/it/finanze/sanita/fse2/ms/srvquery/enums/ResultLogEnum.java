/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.enums;

/** 
 * An enum that represents the result of the operation 
 *
 */
public enum ResultLogEnum implements ILogEnum {

	OK("OK", "Operazione eseguita con successo"),
	KO("KO", "Errore nell'esecuzione dell'operazione"); 

	/** 
	 * The enum code 
	 */
	private String code;
	
	public String getCode() {
		return code;
	}

	/** 
	 * The enum description 
	 */
	private String description;

	private ResultLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

