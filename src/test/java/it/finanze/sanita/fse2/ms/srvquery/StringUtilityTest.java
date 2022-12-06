/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class StringUtilityTest {

	@Test
	void isNullOrEmptyTest() {
		String str = "";
		String nullStr = null;

		assertTrue(StringUtility.isNullOrEmpty(str));
		assertTrue(StringUtility.isNullOrEmpty(nullStr));
	}

	@Test
	void isNullOrEmptyFalse() {
		String str = "Hello World!";

		assertFalse(StringUtility.isNullOrEmpty(str));

	}

	@Test
	void generateTransactionUIDTest() {
		String uuidIpMode = StringUtility.generateTransactionUID(UIDModeEnum.IP_UUID);
		String uuidHostnameMode = StringUtility.generateTransactionUID(UIDModeEnum.HOSTNAME_UUID);
		String uuidIdMode = StringUtility.generateTransactionUID(UIDModeEnum.UUID_UUID);

		assertEquals(String.class, uuidIpMode.getClass());
		assertEquals(String.class, uuidHostnameMode.getClass());
		assertEquals(String.class, uuidIdMode.getClass());

		assertEquals(64, uuidIdMode.length());

	}

}
