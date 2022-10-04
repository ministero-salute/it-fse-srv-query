package it.finanze.sanita.fse2.ms.srvquery.enums;

/** 
 * Event Type Enum 
 *
 */
public enum EventTypeEnum {

	KAFKA("Kafka"),
	GENERIC_ERROR("Generic error from Query");

	/** 
	 * The event name 
	 */
	private String name;

	private EventTypeEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

} 


