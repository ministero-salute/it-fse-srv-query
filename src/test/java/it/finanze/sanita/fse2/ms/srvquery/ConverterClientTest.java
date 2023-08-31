package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.ConverterClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.config.MsUrlCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class ConverterClientTest {

	@MockBean
    private MsUrlCFG msUrlCFG;
	
    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ConverterClient converterClient;

    @BeforeEach
    void setup() {
        when(msUrlCFG.getMsConverterHost()).thenReturn("mock-converter");
    }

    @Test
    void testCallConvertToFhirJson() throws IOException {
    	
        FormatEnum format = FormatEnum.CUSTOM_JSON;
        RequestDTO creationInfo = new RequestDTO();
        byte[] fileBytes = "{\"test\":\"test\"}".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.json", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        ConversionResponseDTO expectedResponse = new ConversionResponseDTO();
        expectedResponse.setResult("Result");

        // Mock restTemplate.postForObject()
        String url = "mock-converter/v1/metadata-resource/from/" + format.toString() + "/to-fhir-json";
        when(restTemplate.postForObject(eq(url), any(HttpEntity.class), eq(ConversionResponseDTO.class))).thenReturn(expectedResponse);

        // Perform callConvertToFhirJson()
        ConversionResponseDTO actualResponse = converterClient.callConvertToFhirJson(format, creationInfo, file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("creationInfo", creationInfo);
        body.add("file", new ByteArrayResource(file.getBytes()));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // Check call to postForObject and assertions
        verify(restTemplate).postForObject(url, entity, ConversionResponseDTO.class);
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getResult(), actualResponse.getResult());
    }

    @Test
    void testCallConvertFromFhirJson() {
        // Mock the necessary dependencies
        FormatEnum format = FormatEnum.FHIR_R4_XML;
        String oid = "123";
        byte[] file = "{\"test\":\"test\"}".getBytes();
        
        ConversionResponseDTO expectedResponse = new ConversionResponseDTO();
        expectedResponse.setResult("Result");
        
        // Mock restTemplate.postForObject()
        String url = msUrlCFG.getMsConverterHost()+"/v1/metadata-resource/from-fhir-json/to/"+format.toString();
        when(restTemplate.postForObject(eq(url), any(HttpEntity.class), eq(ConversionResponseDTO.class))).thenReturn(expectedResponse);

        // Perform callConvertFromFhirJson()
        ConversionResponseDTO actualResponse = converterClient.callConvertFromFhirJson(format, oid, file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // Check call to postForObject and assertions
        verify(restTemplate).postForObject(url, entity, ConversionResponseDTO.class);
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getResult(), actualResponse.getResult());
    }
	
}
