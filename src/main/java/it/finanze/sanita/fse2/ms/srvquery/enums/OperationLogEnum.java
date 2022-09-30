package it.finanze.sanita.fse2.ms.srvquery.enums;

public enum OperationLogEnum implements ILogEnum {

	KAFKA_SENDING_MESSAGE("KAFKA-SENDING-MESSAGE", "Invio Messaggio su Kafka"),
	KAFKA_RECEIVING_MESSAGE("KAFKA-RECEIVING-MESSAGE", "Ricezione Messaggio da Kafka"); 

	
	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

