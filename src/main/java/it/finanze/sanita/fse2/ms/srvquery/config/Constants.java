package it.finanze.sanita.fse2.ms.srvquery.config;

public class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.finanze.sanita.fse2.ms.srvquery";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.finanze.sanita.fse2.ms.srvquery.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.finanze.sanita.fse2.ms.srvquery.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.finanze.sanita.fse2.ms.srvquery.config";

		/** 
		 * This method is intentionally left blank
		 */
		private ComponentScan() {
			
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
