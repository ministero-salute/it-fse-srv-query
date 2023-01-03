/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import com.google.gson.Gson;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DeleteResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ReplaceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.UpdateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
@DisplayName("Query Controller Unit Test")
class DocumentControllerTest {

    public static final String IDENTIFIER = "identifier";

    @Autowired
    MockMvc mvc;

    @Test
    void livenessCheckCtlTest() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpectAll(status().isOk()).andReturn().getResponse();


        Map contentResponse = StringUtility.fromJSONJackson(new String(response.getContentAsByteArray()), Map.class);
        assertTrue(contentResponse.containsKey("status"));
        assertEquals(Status.UP.getCode(), contentResponse.get("status"));
    }

    @Test
    void publishTest() throws Exception {
        String bundle = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "RefertoDiLaboratorioNonITI.json"), StandardCharsets.UTF_8);
        FhirPublicationDTO body = new FhirPublicationDTO();
        body.setJsonString(bundle);
        body.setIdentifier(IDENTIFIER);

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.post("/v1/document/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        CreateResponseDTO contentResponse = StringUtility.fromJSONJackson(new String(response.getContentAsByteArray()), CreateResponseDTO.class);

        assertAll(
                () -> assertFalse(contentResponse.isEsito()),
                () -> assertTrue(StringUtils.isNotEmpty(contentResponse.getMessage()))
        );
    }

    @Test
    void replaceErrorTest() throws Exception {
        String bundle = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "RefertoDiLaboratorioNonITI.json"), StandardCharsets.UTF_8);
        FhirPublicationDTO body = new FhirPublicationDTO();
        body.setJsonString(bundle);
        body.setIdentifier(IDENTIFIER);

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.put("/v1/document/replace")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ReplaceResponseDTO contentResponse = StringUtility.fromJSONJackson(new String(response.getContentAsByteArray()), ReplaceResponseDTO.class);

        assertAll(
                () -> assertFalse(contentResponse.isEsito()),
                () -> assertTrue(StringUtils.isNotEmpty(contentResponse.getMessage()))
        );
    }

    @Test
    void updateErrorTest() throws Exception {
        String bundle = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "RefertoDiLaboratorioNonITI.json"), StandardCharsets.UTF_8);
        FhirPublicationDTO body = new FhirPublicationDTO();
        body.setJsonString(bundle);
        body.setIdentifier(IDENTIFIER);

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.put("/v1/document/metadata/" + IDENTIFIER)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new Gson().toJson(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        UpdateResponseDTO contentResponse = StringUtility.fromJSONJackson(new String(response.getContentAsByteArray()), UpdateResponseDTO.class);

        assertAll(
                () -> assertFalse(contentResponse.isEsito()),
                () -> assertTrue(StringUtils.isNotEmpty(contentResponse.getMessage()))
        );
    }

    @Test
    void deleteErrorTest() throws Exception {
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.delete("/v1/document/delete/" + IDENTIFIER))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        DeleteResponseDTO contentResponse = StringUtility.fromJSONJackson(new String(response.getContentAsByteArray()), DeleteResponseDTO.class);

        assertAll(
                () -> assertFalse(contentResponse.isEsito()),
                () -> assertTrue(StringUtils.isNotEmpty(contentResponse.getMessage()))
        );
    }

    @Test
    void checkExistErrorTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/document/check-exist/" + IDENTIFIER))
                .andExpect(status().isInternalServerError());
    }
}