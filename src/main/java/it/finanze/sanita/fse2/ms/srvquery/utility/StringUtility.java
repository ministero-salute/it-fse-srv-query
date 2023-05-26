/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isWhitespace;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtility {

	private static final String MASTER_ID_SEPARATOR = "^";
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
}

