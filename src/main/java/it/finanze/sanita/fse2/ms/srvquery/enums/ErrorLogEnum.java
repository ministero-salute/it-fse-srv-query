/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.enums;

/** 
 * Error Code for Structured Logs 
 *
 */
public enum ErrorLogEnum implements ILogEnum {

	KO_KAFKA_SEND_MESSAGE("KO-KAFKA-SEND-MESSAGE", "Error while sending message on Kafka topic"),
	KO_KAFKA_RECEIVE_MESSAGE("KO-KAFKA-RECEIVE-MESSAGE", "Error while retrieving message from Kafka topic");

	/** 
	 * The error code 
	 */
	private String code;
	
	public String getCode() {
		return code;
	}

	/** 
	 * The error description 
	 */
	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

