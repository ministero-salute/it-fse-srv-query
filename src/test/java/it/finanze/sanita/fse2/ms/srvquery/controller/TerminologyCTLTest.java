package it.finanze.sanita.fse2.ms.srvquery.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.controller.impl.TerminologyCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class TerminologyCTLTest {

	@Autowired
    private TerminologyCTL terminologyCTL;
	
	@MockBean
    private ITerminologySRV terminologySRV;

    @MockBean
    private MockHttpServletRequest request;

    @Test
    void testUploadTerminology() throws IOException {
        FormatEnum format = FormatEnum.CUSTOM_JSON;
        RequestDTO creationInfo = new RequestDTO();
        MockMultipartFile file = new MockMultipartFile("file", "terminology.json", "application/json", "{\"test\":\"test\"}".getBytes());

        UploadResponseDTO expectedResponse = new UploadResponseDTO();
        expectedResponse.setInsertedItems(1);

        when(terminologySRV.uploadTerminology(format, creationInfo, file)).thenReturn(expectedResponse);

        UploadResponseDTO actualResponse = terminologyCTL.uploadTerminology(format, creationInfo, file, request);

        // Assertions
        assertEquals(expectedResponse, actualResponse);
        verify(terminologySRV, times(1)).uploadTerminology(format, creationInfo, file);
    }

    @Test
    void testGetTerminology() {
        String oid = "terminology_oid";
        String version = "1.0";

        GetResponseDTO expectedResponse = new GetResponseDTO();
        expectedResponse.setPresent(true);
        expectedResponse.setCounter(1);

        when(terminologySRV.isPresent(oid, version)).thenReturn(expectedResponse);

        GetResponseDTO actualResponse = terminologyCTL.getTerminology(oid, version, request);

        // Assertions
        assertEquals(expectedResponse, actualResponse);
        verify(terminologySRV, times(1)).isPresent(oid, version);
    }

    @Test
    void testDeleteTerminology() {
        String idResource = "terminology_id";

        terminologyCTL.deleteTerminology(idResource, request);

        // Verify call to service
        verify(terminologySRV, times(1)).deleteById(idResource);
    }
	
}
