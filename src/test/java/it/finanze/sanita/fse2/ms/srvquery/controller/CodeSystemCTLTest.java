package it.finanze.sanita.fse2.ms.srvquery.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.controller.impl.CodeSystemCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.GetActiveResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetActiveResourceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class CodeSystemCTLTest {

	@MockBean
    private ITerminologySRV service;

	@Autowired
    private CodeSystemCTL controller;

    @Test
    void testInsertCodeSystem() {
        CreateCodeSystemReqDTO dto = new CreateCodeSystemReqDTO();
        LogTraceInfoDTO traceInfoDTO = new LogTraceInfoDTO("spanId", "traceId");
        CreateCodeSystemResDTO expectedResponse = new CreateCodeSystemResDTO(traceInfoDTO, "id");

        // Mock terminologySRV.manageCodeSystem()
        when(service.manageCodeSystem(dto)).thenReturn(expectedResponse);

        // Perform insertCodeSystem()
        ResponseEntity<CreateCodeSystemResDTO> actualResponse = controller.insertCodeSystem(dto);

        assertEquals(expectedResponse, actualResponse.getBody());
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    void testGetActiveResource() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        List<GetActiveResourceDTO> expectedList = new ArrayList<>();
        GetActiveResourceResponseDTO expectedResponse = new GetActiveResourceResponseDTO(new LogTraceInfoDTO(null, null), expectedList);

        // Mock terminologySRV.getSummaryNameActiveResource()
        when(service.getSummaryNameActiveResource()).thenReturn(expectedList);

        // Perform getActiveResource()
        GetActiveResourceResponseDTO actualResponse = controller.getActiveResource(request);

        assertEquals(expectedResponse.getActiveResources(), actualResponse.getActiveResources());
    }

    @Test
    void testGetResource() {
        String id = "resourceId";
        FormatEnum format = FormatEnum.CUSTOM_JSON;
        HttpServletRequest request = mock(HttpServletRequest.class);
        GetResDTO expectedResponse = new GetResDTO();

        // Mock terminologySRV.export()
        when(service.export(id, format)).thenReturn(expectedResponse);

        // Perform getResource()
        GetResDTO actualResponse = controller.getResource(id, format, request);

        assertEquals(expectedResponse, actualResponse);

    }
	
    @Test
    void getValuesetFromCSAndExpandTest() {
    	String loincOID = "urn:oid:2.16.840.1.113883.5.1";
    	service.expandValuesetAfterChangeCodeySystem(loincOID);
    }
}
