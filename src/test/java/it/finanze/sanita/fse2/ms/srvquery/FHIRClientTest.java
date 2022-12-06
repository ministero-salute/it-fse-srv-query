/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class FHIRClientTest {

	private FHIRClient fhirClient;

	@Autowired
	private FhirCFG fhircfg;
	

	@BeforeEach
	void init() {
		fhirClient = new FHIRClient(fhircfg.getFhirServerUrl(),fhircfg.getFhirServerUser(), fhircfg.getFhirServerPwd());
	}

	@Test
	void bulkPublishTest() {
		String bundleString = new String(FileUtility.getFileFromInternalResources("RefertoDiLaboratorioNonITI.json"));
		Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, bundleString, true);
		boolean create = fhirClient.create(bundle);
		assertTrue(create);
		String bundleStringReplace = new String(FileUtility.getFileFromInternalResources("RefertoDiLaboratorioToReplace.json"));
		Bundle bundleReplace = FHIRR4Helper.deserializeResource(Bundle.class, bundleStringReplace, true);
		boolean replace = fhirClient.replace(bundleReplace);
		assertTrue(create);
	}
}
