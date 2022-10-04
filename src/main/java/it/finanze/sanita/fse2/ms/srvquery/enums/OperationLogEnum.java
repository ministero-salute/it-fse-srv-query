package it.finanze.sanita.fse2.ms.srvquery.enums;

/** 
 * The operation in Structured Logs 
 *
 */
public enum OperationLogEnum implements ILogEnum {

	KAFKA_SENDING_MESSAGE("KAFKA-SENDING-MESSAGE", "Invio Messaggio su Kafka"),
	KAFKA_RECEIVING_MESSAGE("KAFKA-RECEIVING-MESSAGE", "Ricezione Messaggio da Kafka"); 

	/** 
	 * The operation code 
	 */
	private String code;
	
	public String getCode() {
		return code;
	}

	/** 
	 * The operation description 
	 */
	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

