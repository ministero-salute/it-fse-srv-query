package it.finanze.sanita.fse2.ms.srvquery.enums;

public enum ErrorLogEnum implements ILogEnum {

	KO_KAFKA_SEND_MESSAGE("KO-KAFKA-SEND-MESSAGE", "Error while sending message on Kafka topic"),
	KO_KAFKA_RECEIVE_MESSAGE("KO-KAFKA-RECEIVE-MESSAGE", "Error while retrieving message from Kafka topic");

	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

