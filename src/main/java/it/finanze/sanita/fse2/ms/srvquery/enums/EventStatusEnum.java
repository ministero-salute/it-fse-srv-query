/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.enums;

/**
 * Retrieves the Event Status 
 *
 */
public enum EventStatusEnum {

	SUCCESS("Success"), 
	ERROR("Error");

	/** 
	 * The event name 
	 */
	private String name;

	private EventStatusEnum(String inName) {
		name = inName;
	}

	public String getName() {
		return name;
	}

} 
