/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.config;

public class Constants {

	
	/** 
	 * Contains the constants for the launch profile of the SpringBoot application. 
	 * 
	 */
	public static final class Profile {
		
		/**
		 * Test profile
		 */
		public static final String TEST = "TEST"; 
		
		/** 
		 * Test Prefix 
		 */
		public static final String TEST_PREFIX = "test"; 

		/**
		 * Dev profile.
		 */
		public static final String DEV = "dev";

		/** 
		 * Constructor. This method is intentionally left blank.
		 */
		private Profile() {

		}

	}
	
	/**
	 * Constants used in logging. 
	 *
	 */
	public static final class Logs {
		
		/** 
		 * When there is an error retrieving the host info
		 */
		public static final String ERROR_RETRIEVING_HOST_INFO = "Error while retrieving host informations"; 

		/** 
		 * This method is intentionally left blank
		 */	
		private Logs() {

		}

	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}
}
