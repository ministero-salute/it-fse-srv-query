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
package it.finanze.sanita.fse2.ms.srvquery;

import static it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility.getSearchParamFromMasterId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
class UtiltyTest {
    
    @Test
    void searchParamsTest() {
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId(null));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId(""));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^ab"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("ab^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("ab^^cd"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  ^  "));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^  "));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  ^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  "));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^^^^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  ^^  ^^"));
        assertEquals("cd", getSearchParamFromMasterId("ab^cd"));
        assertEquals("abcd", getSearchParamFromMasterId("abcd"));
        assertEquals("UAT_GTW_ID162", getSearchParamFromMasterId("2.16.840.4^UAT_GTW_ID162"));
    }

    @Test
    void serializationTest() {

        byte[] jsonFhir = FileUtility.getFileFromInternalResources("Files/CreationJsonFhir.json");
        String json = new String(jsonFhir, StandardCharsets.UTF_8);
        Object obj = FHIRR4Helper.deserializeResource(Bundle.class, json, true);

        assertDoesNotThrow(() -> FHIRR4Helper.serializeResource((Bundle) obj, true, true, true));
    }
    
    @ParameterizedTest
	@NullAndEmptySource
	@DisplayName("isNullOrEmpty String Test")
	void isNullOrEmptyStringTest(String str) {
		assertTrue(StringUtility.isNullOrEmpty(str));
		assertFalse(StringUtility.isNullOrEmpty("notEmpty"));
	}

    @Test
    void generateRandomBundle() {
        byte[] bundleLDO = FileUtility.getFileFromInternalResources("Files" + File.separator + "BundleLDO.json");
        System.out.println(new String(bundleLDO));
    }
}
