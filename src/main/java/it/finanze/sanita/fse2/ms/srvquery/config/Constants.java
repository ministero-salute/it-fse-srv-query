/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
        public static final String SECURITY_CODE_RESTRICTED = "R";
        public static final String SECURITY_CODE_VERY_RESTRICTED = "V";

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
