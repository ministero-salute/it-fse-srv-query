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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DeleteResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ReplaceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.UpdateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
@DisplayName("Query Controller Unit Test")
class DocumentControllerTest {

	static final String IDENTIFIER = "identifier";

	@Autowired
	MockMvc mvc;

	@MockBean
	IFHIRSRV fhirSRV;

	@Test
	@SuppressWarnings("rawtypes")
	void livenessCheckCtlTest() throws Exception {
		MockHttpServletResponse response = mvc.perform(get("/status")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpectAll(status().isOk()).andReturn().getResponse();

		Map contentResponse = StringUtility.fromJSONJackson(new String(response.getContentAsByteArray()),
				Map.class);
		assertTrue(contentResponse.containsKey("status"));
		assertEquals(Status.UP.getCode(), contentResponse.get("status"));
	}

	@Test
	void publishTest() throws Exception {
		String bundle = new String(
				FileUtility.getFileFromInternalResources(
						"Files" + File.separator + "RefertoDiLaboratorioNonITI.json"),
				StandardCharsets.UTF_8);
		FhirPublicationDTO body = new FhirPublicationDTO();
		body.setJsonString(bundle);
		body.setIdentifier(IDENTIFIER);

		when(fhirSRV.create(any(FhirPublicationDTO.class))).thenReturn(true);

		MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.post("/v1/document/create")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(new Gson().toJson(body)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		CreateResponseDTO contentResponse = StringUtility
				.fromJSONJackson(new String(response.getContentAsByteArray()), CreateResponseDTO.class);

		assertAll(
				() -> assertTrue(contentResponse.isEsito()),
				() -> assertFalse(StringUtils.isNotEmpty(contentResponse.getMessage())));
	}

	@Test
	void replaceTest() throws Exception {
		String bundle = new String(
				FileUtility.getFileFromInternalResources(
						"Files" + File.separator + "RefertoDiLaboratorioNonITI.json"),
				StandardCharsets.UTF_8);
		FhirPublicationDTO body = new FhirPublicationDTO();
		body.setJsonString(bundle);
		body.setIdentifier(IDENTIFIER);

		when(fhirSRV.replace(any(FhirPublicationDTO.class))).thenReturn(true);

		MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.put("/v1/document/replace")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(new Gson().toJson(body)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		ReplaceResponseDTO contentResponse = StringUtility.fromJSONJackson(
				new String(response.getContentAsByteArray()), ReplaceResponseDTO.class);

		assertAll(
				() -> assertTrue(contentResponse.isEsito()),
				() -> assertFalse(StringUtils.isNotEmpty(contentResponse.getMessage())));
	}

	@Test
	void errorReplaceTest() throws Exception {
		String bundle = new String(
				FileUtility.getFileFromInternalResources(
						"Files" + File.separator + "RefertoDiLaboratorioNonITI.json"),
				StandardCharsets.UTF_8);
		FhirPublicationDTO body = new FhirPublicationDTO();
		body.setJsonString(bundle);
		body.setIdentifier(IDENTIFIER);

		when(fhirSRV.replace(any(FhirPublicationDTO.class))).thenThrow(new BusinessException("error"));

		MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.put("/v1/document/replace")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(new Gson().toJson(body)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		ReplaceResponseDTO contentResponse = StringUtility.fromJSONJackson(
				new String(response.getContentAsByteArray()), ReplaceResponseDTO.class);

		assertAll(
				() -> assertFalse(contentResponse.isEsito()),
				() -> assertTrue(StringUtils.isNotEmpty(contentResponse.getMessage())));
	}

	@Test
	void updateTest() throws Exception {
		String bundle = new String(
				FileUtility.getFileFromInternalResources(
						"Files" + File.separator + "RefertoDiLaboratorioNonITI.json"),
				StandardCharsets.UTF_8);
		FhirPublicationDTO body = new FhirPublicationDTO();
		body.setJsonString(bundle);
		body.setIdentifier(IDENTIFIER);

		when(fhirSRV.updateMetadata(any(FhirPublicationDTO.class))).thenReturn(true);

		MockHttpServletResponse response = mvc
				.perform(MockMvcRequestBuilders.put("/v1/document/metadata/" + IDENTIFIER)
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(new Gson().toJson(body)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		UpdateResponseDTO contentResponse = StringUtility
				.fromJSONJackson(new String(response.getContentAsByteArray()), UpdateResponseDTO.class);

		assertAll(
				() -> assertTrue(contentResponse.isEsito()),
				() -> assertFalse(StringUtils.isNotEmpty(contentResponse.getMessage())));
	}

	@Test
	void errorUpdateTest() throws Exception {
		String bundle = new String(
				FileUtility.getFileFromInternalResources(
						"Files" + File.separator + "RefertoDiLaboratorioNonITI.json"),
				StandardCharsets.UTF_8);
		FhirPublicationDTO body = new FhirPublicationDTO();
		body.setJsonString(bundle);
		body.setIdentifier(IDENTIFIER);

		when(fhirSRV.updateMetadata(any(FhirPublicationDTO.class))).thenThrow(new BusinessException("error"));

		MockHttpServletResponse response = mvc
				.perform(MockMvcRequestBuilders.put("/v1/document/metadata/" + IDENTIFIER)
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(new Gson().toJson(body)))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		UpdateResponseDTO contentResponse = StringUtility
				.fromJSONJackson(new String(response.getContentAsByteArray()), UpdateResponseDTO.class);

		assertAll(
				() -> assertFalse(contentResponse.isEsito()),
				() -> assertTrue(StringUtils.isNotEmpty(contentResponse.getMessage())));
	}

	@Test
	void deleteTest() throws Exception {

		when(fhirSRV.delete(IDENTIFIER)).thenReturn(true);

		MockHttpServletResponse response = mvc
				.perform(MockMvcRequestBuilders.delete("/v1/document/delete/" + IDENTIFIER))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		DeleteResponseDTO contentResponse = StringUtility
				.fromJSONJackson(new String(response.getContentAsByteArray()), DeleteResponseDTO.class);

		assertAll(
				() -> assertTrue(contentResponse.isEsito()),
				() -> assertFalse(StringUtils.isNotEmpty(contentResponse.getMessage())));
	}

	@Test
	void errorDeletionTest() throws Exception {
		when(fhirSRV.delete("mockError")).thenThrow(new BusinessException("Mock error"));
		MockHttpServletResponse response = mvc
				.perform(MockMvcRequestBuilders.delete("/v1/document/delete/" + "mockError"))
				.andExpect(status().isOk())
				.andReturn().getResponse();

		DeleteResponseDTO errorResponse = StringUtility
				.fromJSONJackson(new String(response.getContentAsByteArray()), DeleteResponseDTO.class);

		assertAll(
				() -> assertFalse(errorResponse.isEsito()),
				() -> assertTrue(StringUtils.isNotEmpty(errorResponse.getMessage())));
	}

	@Test
	void checkExistErrorTest() throws Exception {

		when(fhirSRV.checkExists(anyString())).thenThrow(new BusinessException("Error"));

		mvc.perform(MockMvcRequestBuilders.get("/v1/document/check-exist/" + IDENTIFIER))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void checkExistOkTest() throws Exception {

		when(fhirSRV.checkExists(anyString())).thenReturn(true);

		mvc.perform(MockMvcRequestBuilders.get("/v1/document/check-exist/" + IDENTIFIER))
				.andExpect(status().isOk());
	}
}
