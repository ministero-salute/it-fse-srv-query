/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.config;

public class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.sanita.srvquery";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.sanita.srvquery.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.sanita.srvquery.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.sanita.srvquery.config";

		/** 
		 * This method is intentionally left blank
		 */
		private ComponentScan() {
			
		}

	}
	
	/** 
	 * Contains all the constants used in the headers of HTTP calls 
	 */
	public static final class Headers {
		
		/**
		 * JWT header field.
		 */
		public static final String JWT_HEADER = "Authorization";

		/** 
		 * This method is intentionally left blank
		 */
		private Headers() {

		}

	}
 
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
	 * Generic Constants used in the application. 
	 *
	 */
	public static final class App {
		
		/** 
		 * The JWT Token Audience 
		 */
		public static final String JWT_TOKEN_AUDIENCE = "eds-srv-query";

		/** 
		 * The JWT Token Type 
		 */
		public static final String JWT_TOKEN_TYPE = "JWT";

		/** 
		 * The JWT Bearer Prefix
		 */
		public static final String BEARER_PREFIX = "Bearer ";
		
		/** 
		 * Deleted field 
		 */
		public static final String DELETED = "deleted"; 
		
		/** 
		 * SHA-256 
		 */
		public static final String SHA_256 = "SHA-256"; 
		
		/** 
		 * Identifier field 
		 */
		public static final String IDENTIFIER = "identifier";

		/** 
		 * Constructor. This method is intentionally left blank.
		 */
		private App() {

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
