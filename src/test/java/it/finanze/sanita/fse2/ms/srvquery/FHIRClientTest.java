/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.enums.UIDModeEnum;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
@Slf4j
class FHIRClientTest {

	private FHIRClient fhirClient;

	@Autowired
	private FhirCFG fhircfg;

	@BeforeEach
	void init() {
		this.fhirClient = new FHIRClient("fhir-server-test-url");
	}

	@Test
	@Disabled("Bulk test")
	void bulkPublishTest() {
		for (int i = 0; i < 5000; i++) {
			String transactionId = StringUtility.generateTransactionUID(UIDModeEnum.UUID_UUID);
			String modifiedBundle = TestConstants.TEST_BUNDLE.replace(TestConstants.PLACEHOLDER, transactionId);
			Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, modifiedBundle, true);
			assertDoesNotThrow(() -> fhirClient.saveBundleWithTransaction(bundle));
			log.info("Published: {}", transactionId);
		}
	}
}
