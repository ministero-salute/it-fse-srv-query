package it.finanze.sanita.fse2.ms.srvquery.enums;

public enum EventTypeEnum {

	KAFKA("Kafka"),
	GENERIC_ERROR("Generic error from Query");

	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

} 


