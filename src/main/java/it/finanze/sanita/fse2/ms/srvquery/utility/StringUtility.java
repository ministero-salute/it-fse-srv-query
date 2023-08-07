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
package it.finanze.sanita.fse2.ms.srvquery.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isWhitespace;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtility {

	private static final String MASTER_ID_SEPARATOR = "^";
	
	private static final String REGEX_OID = "urn:oid:[0-2](\\.(0|[1-9][0-9]*))+";
	/**
	 * The allowed chars are: [a-zA-Z0-9_.]
	 * It's expected a string shape as follows: [chars][^][chars]
	 */
	private static final Pattern MASTER_ID_PTT = Pattern.compile("^[\\w.]+\\^[\\w.]+$");

	private static ObjectMapper objectMapper = new ObjectMapper();

	public static <T> T fromJSON(final String json, final Class<T> cls) {
		return new Gson().fromJson(json, cls);
	}

	/**
	 * Transformation from Object to Json.
	 *
	 * @param obj object to transform
	 * @return json
	 */
	public static String toJSON(final Object obj) {
		return new Gson().toJson(obj);
	}

	/**
	 * Return the extracted search parameter from the master identifier
	 *
	 * @param id The master identifier
	 * @return The search parameter extracted from the parameter or the whole string if none found
	 * @throws IllegalArgumentException If the id is null, empty or malformed
	 */
	public static String getSearchParamFromMasterId(String id) {
		// Working var
		String param;
		// Check argument consistency
		if (id == null || id.isEmpty() || isWhitespace(id)) {
			throw new IllegalArgumentException("The id string is null or empty");
		}
		// If there is no occurrence returns string as whole
		if(!id.contains(MASTER_ID_SEPARATOR)) {
			param = id;
		} else if(MASTER_ID_PTT.matcher(id).matches()) {
			// It's required at least another word after separator
			// No need to fear IndexOutOfBoundsException
			param = id.substring(id.indexOf(MASTER_ID_SEPARATOR) + 1);
			// Check for emptiness
			if(param.isEmpty() || isWhitespace(param)) {
				throw new IllegalArgumentException("The param string is empty");
			}
		} else {
			throw new IllegalArgumentException("The id string is malformed");
		}
		// Return value
		return param;
	}

	public static <T> T fromJSONJackson(String jsonString, Class<T> clazz) {
		T obj = null;
		try {
			obj = objectMapper.readValue(jsonString, clazz);
		} catch (Exception e) {
			log.error("Errore durante la conversione da stringa json a oggetto: " + e);
		}

		return obj;
	}
	
	/**
	 * Returns {@code true} if the String passed as parameter is null or empty.
	 * 
	 * @param str	String to validate.
	 * @return		{@code true} if the String passed as parameter is null or empty.
	 */
	public static boolean isNullOrEmpty(final String str) {
		return str == null || str.isEmpty();
	}
	
	public static String removeUrnOidFromSystem(String oid) {
	    String prefix = "urn:oid:";
	    if (!isNullOrEmpty(oid) && oid.startsWith(prefix)) {
	        return oid.substring(prefix.length());
	    }
	    return oid;
	}
	
	public static boolean validateOid(final String input) {
		Pattern pattern = Pattern.compile(REGEX_OID);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
}

