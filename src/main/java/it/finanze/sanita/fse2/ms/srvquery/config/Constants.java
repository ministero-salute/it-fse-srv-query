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

		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Headers {
		
		/**
		 * JWT header field.
		 */
		public static final String JWT_HEADER = "Authorization";

		private Headers() {
			//This method is intentionally left blank.
		}

	}
 
	public static final class Profile {
		
		/**
		 * Test profile
		 */
		public static final String TEST = "TEST";
		public static final String TEST_PREFIX = "test"; 

		/**
		 * Dev profile.
		 */
		public static final String DEV = "dev";


		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class App {
		
		public static final String JWT_TOKEN_AUDIENCE = "eds-srv-query";

		public static final String JWT_TOKEN_TYPE = "JWT";

		public static final String BEARER_PREFIX = "Bearer ";
		
		public static final String DELETED = "deleted"; 
		
		public static final String SHA_256 = "SHA-256"; 
		
		public static final String IDENTIFIER = "identifier";


		private App() {
			//This method is intentionally left blank.
		}
	}
	
	public static final class Logs {

		
		public static final String ELASTIC_LOGGER_APP_NAME = "application"; 
		 
		public static final String ELASTIC_LOGGER_OP_NAME = "operation"; 

		public static final String ELASTIC_LOGGER_OP_TIMESTAMP = "op-log-timestamp"; 

		public static final String ELASTIC_LOGGER_OP_RESULT = "op-result"; 
		 
		public static final String ELASTIC_LOGGER_OP_TIMESTAMP_START = "op-timestamp-start"; 
		 
		public static final String ELASTIC_LOGGER_OP_TIMESTAMP_END = "op-timestamp-end"; 
		 
		public static final String ELASTIC_LOGGER_OP_ERROR_CODE = "op-error"; 
		 
		public static final String ELASTIC_LOGGER_OP_ERROR_DESCRIPTION = "op-error-description"; 
		
		public static final String ERROR_RETRIEVING_HOST_INFO = "Error while retrieving host informations"; 

		
		private Logs() {
				//This method is intentionally left blank. 
			}

	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}
}
