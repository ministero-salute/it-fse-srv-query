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
		public static final String TEST = "test";
		
		/** 
		 * Test Prefix 
		 */
		public static final String TEST_PREFIX = "test_";

		/**
		 * Dev profile.
		 */
		public static final String DEV = "dev";
		
		/**
		 * Docker profile.
		 */
		public static final String DOCKER = "docker";

		/** 
		 * Constructor. This method is intentionally left blank.
		 */
		private Profile() {

		}

	}
	
	 /**
     * Resource
     */
    public static final class Resource {

        /**
         * Copyright
         */
        public static final String COPYRIGHT = "The code system and its contents cannot be published without proper authorization and may have specific usage restrictions.";
        
        /**
         * Copyright
         */
        public static final String SECURITY_SYSTEM= "https://www.hl7.org/fhir/R4/v3/ConfidentialityClassification/vs.html";
        public static final String SECURITY_CODE= "R";
        public static final String SECURITY_DISPLAY= "Restricted";

        /**
         * Private constructor to disallow to access from other classes
         */
        private Resource() {}
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
        public static final String START_LOG = "[START] {}() with arguments {}={}";
		public static final String CREATE = "create";
		public static final String TRACE_ID = "traceId";
		public static final String EXIT_LOG = "[EXIT] {}() with arguments {}={}";
		public static final String EXIST = "exist";
		public static final String UPDATE = "update";

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
