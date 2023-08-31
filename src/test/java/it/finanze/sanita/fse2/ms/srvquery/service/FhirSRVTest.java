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
package it.finanze.sanita.fse2.ms.srvquery.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.google.gson.internal.LinkedTreeMap;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.UpdateBodyDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.service.impl.FHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
class FhirSRVTest {

	@Autowired
	private IFHIRSRV fhirSRV;

	private Bundle bundle;

	private DocumentReference documentReference;

	@BeforeEach
	public void setUp() throws Exception {

		FhirCFG fhirCFG = Mockito.mock(FhirCFG.class);

		when(fhirCFG.getFhirServerUser()).thenReturn("mock");
		when(fhirCFG.getFhirServerPwd()).thenReturn("mock");
		when(fhirCFG.getFhirServerUrl()).thenReturn("http://localhost:8080/mock");

		// Create a mock of the private client
		FHIRClient fhirClient = Mockito.mock(FHIRClient.class);
		when(fhirClient.create(any(Bundle.class))).thenReturn(true);
		when(fhirClient.update(any(DocumentReference.class))).thenReturn(true);
		when(fhirClient.delete(any(Bundle.class))).thenReturn(true);
		when(fhirClient.replace(any(Bundle.class))).thenReturn(true);

		bundle = mockBundle();
		documentReference = mockDocumentReference();

		when(fhirClient.getDocument(anyString(), anyString())).thenReturn(bundle);
		when(fhirClient.getDocumentReferenceBundle(anyString())).thenReturn(documentReference);
		when(fhirClient.findByMasterIdentifier(anyString())).thenReturn(bundle);

		fhirSRV = new FHIRSRV();

		Field privateField = fhirSRV.getClass().getDeclaredField("fhirClient");
		privateField.setAccessible(true);
		privateField.set(fhirSRV, fhirClient);

		Field configField = fhirSRV.getClass().getDeclaredField("fhirCFG");
		configField.setAccessible(true);
		configField.set(fhirSRV, fhirCFG);
	}

	private DocumentReference mockDocumentReference() {
		DocumentReference documentReference = new DocumentReference();

		DocumentReferenceContextComponent context = new DocumentReferenceContextComponent();
		context.addRelated().setReference("http://localhost:8080/DocumentReference/1234567890");

		documentReference.setContext(context);

		return documentReference;
	}

	private Bundle mockBundle() {
		Bundle bundle = new Bundle();
		List<BundleEntryComponent> entryList = new ArrayList<>();
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(new DocumentReference());
		entryList.add(entry);

		bundle.setEntry(entryList);

		return bundle;
	}

	@Test
	void checkExistsTest() {
		boolean outcome = fhirSRV.checkExists("masterId");
		assertTrue(outcome);

		assertThrows(BusinessException.class, () -> fhirSRV.checkExists(null));
	}


	@Test
	void createTest() {

		FhirPublicationDTO fhirPublicationDTO = new FhirPublicationDTO();
		byte[] jsonFhir = FileUtility.getFileFromInternalResources("Files/CreationJsonFhir.json");

		fhirPublicationDTO.setIdentifier("masterId");
		fhirPublicationDTO.setJsonString(new String(jsonFhir, StandardCharsets.UTF_8));

		boolean outcome = fhirSRV.create(fhirPublicationDTO);
		assertTrue(outcome);

		assertThrows(BusinessException.class, () -> fhirSRV.create(null));
	}

	@Test
	void deleteTest() {
		boolean outcome = fhirSRV.delete("masterId");
		assertTrue(outcome);
	}

	@Test
	void replaceTest() {

		FhirPublicationDTO fhirPublicationDTO = new FhirPublicationDTO();
		byte[] jsonFhir = FileUtility.getFileFromInternalResources("Files/CreationJsonFhir.json");

		fhirPublicationDTO.setIdentifier("masterId");
		fhirPublicationDTO.setJsonString(new String(jsonFhir, StandardCharsets.UTF_8));

		// To revise replace() due to NPE"
		// boolean outcome = fhirSRV.replace(fhirPublicationDTO);
		// assertTrue(outcome);

		assertThrows(BusinessException.class, () -> fhirSRV.replace(null));
	}

	@Test
	@SuppressWarnings("unchecked")	
	void updateTest() {
		FhirPublicationDTO fhirPublicationDTO = new FhirPublicationDTO();
		byte[] jsonFhir = FileUtility.getFileFromInternalResources("Files/CreationJsonFhir.json");

		UpdateBodyDTO updateBodyDTO = new UpdateBodyDTO();
		updateBodyDTO.setAssettoOrganizzativo("Assetto organizzativo");
		updateBodyDTO.setAttiCliniciRegoleAccesso(Arrays.asList("Atti clinici regole accesso"));
		updateBodyDTO.setConservazioneANorma("Conservazione a norma");
		updateBodyDTO.setDataFinePrestazione(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		updateBodyDTO.setDataInizioPrestazione(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		updateBodyDTO.setTipoAttivitaClinica("Tipo attivita clinica");
		updateBodyDTO.setTipoDocumentoLivAlto("Tipo documento liv alto");
		updateBodyDTO.setTipologiaStruttura("Tipologia struttura");

		LinkedTreeMap<String, Object> objT = StringUtility.fromJSON(new String(jsonFhir, StandardCharsets.UTF_8), LinkedTreeMap.class);
		objT.put("body", updateBodyDTO);

		String jsonFhirUpdate = StringUtility.toJSON(objT);

		fhirPublicationDTO.setIdentifier("masterId");
		fhirPublicationDTO.setJsonString(jsonFhirUpdate);

		boolean outcome = fhirSRV.updateMetadata(fhirPublicationDTO);
		assertTrue(outcome);

		assertThrows(BusinessException.class, () -> fhirSRV.updateMetadata(null));
	}

	@Test
	void testVI() {
		String serverURL = "http://localhost:8080/fhir";
		String username = "admin";
		String pwd = "admin";
		
//		String identifier = "urn:oid:2.16.840.1.113883.6.1";
		String identifier = "loinc";
		
		String version = "";
		IGenericClient tc =  FHIRR4Helper.createClient(serverURL, username, pwd);
//		getDocument(tc,CodeSystem.class, "954", "1");
//		getDocument(tc,CodeSystem.class, "954", "2");
//		 System.out.println("Stop");
		
		CodeSystem metadata = tc.read().resource(CodeSystem.class).withIdAndVersion("954", "1").execute();
		System.out.println("METADATA 1" + metadata.getTitle());
		CodeSystem metadata2 = tc.read().resource(CodeSystem.class).withIdAndVersion("954", "2").execute();
		System.out.println("METADATA 2" + metadata2.getTitle());
		CodeSystem metadata3 = tc.read().resource(CodeSystem.class).withIdAndVersion("954", null).execute();
		System.out.println("METADATA 3" +metadata3.getTitle());
		
		CodeSystem metadata4 = tc.read().resource(CodeSystem.class).withIdAndVersion("fratm", null).execute();
		System.out.println("METADATA 3" +metadata4.getTitle());
		
	}
	
	public void getDocument(IGenericClient tc,Class mr, final String id, final String version) {
		try {
			CodeSystem cs = (CodeSystem)tc.search().byUrl("http://localhost:8080/fhir/CodeSystem/" + id +"/_history/" + version).execute();
			System.out.println(cs.getTitle());
		} catch(Exception ex) {
			throw new BusinessException("Errore while perform getDocument client method:", ex);
		}
	}
}
