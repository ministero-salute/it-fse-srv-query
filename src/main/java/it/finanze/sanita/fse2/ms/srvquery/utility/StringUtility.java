/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class StringUtility {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private StringUtility() {
		// Constructor intentionally empty.
	}

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

	public static String getSearchParameterFromMasterIdentifier(final String masterIdentifier) {
		String searchParameter = masterIdentifier;
		if (masterIdentifier.contains("^")) {
			searchParameter = masterIdentifier.split("\\^")[1];
		}
		return searchParameter;
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
}

