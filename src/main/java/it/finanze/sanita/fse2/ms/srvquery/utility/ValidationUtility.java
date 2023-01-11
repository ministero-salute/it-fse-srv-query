/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A Utility Class that contains the definition of some constant values 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtility {
	
	public static final int DEFAULT_STRING_MIN_SIZE = 0;
	public static final int DEFAULT_STRING_MAX_SIZE = 100;
	public static final int DEFAULT_NUMBER_MIN_SIZE = 0;
	public static final int DEFAULT_NUMBER_MAX_SIZE = 10000;
	public static final int DEFAULT_ARRAY_MIN_SIZE = 0;
	public static final int DEFAULT_ARRAY_MAX_SIZE = 10000;
	public static final int DEFAULT_BINARY_MIN_SIZE = 0;
	public static final int DEFAULT_BINARY_MAX_SIZE = 10000;
	
} 
