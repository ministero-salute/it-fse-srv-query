package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TranslatorClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.controller.impl.MetadataResourceCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.MetadataResourceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.ResultPushEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class MetadataResourceCTLTest {

	@Mock
	private Tracer tracer;
	@Mock
    private ITerminologySRV terminologySRV;
	@Mock
	private TerminologyClient terminologyClient;
	@Mock
	private TranslatorClient translatorClient;
	@InjectMocks
    private MetadataResourceCTL metadataResourceCTL;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testManageMetadataResource() {
    	SystemUrlDTO system = new SystemUrlDTO();
    	system.setSystem("system");
        List<SystemUrlDTO> systems = new ArrayList<>();
        systems.add(system);

        MetadataResourceDTO singleResult = new MetadataResourceDTO();
        singleResult.setSystem("system");
        singleResult.setEsito(ResultPushEnum.SAVED);
        List<MetadataResourceDTO> expectedResult = new ArrayList<>();
        expectedResult.add(singleResult);

        when(terminologySRV.manageMetadataResource(systems)).thenReturn(expectedResult);

        MetadataResourceResponseDTO result = metadataResourceCTL.manageMetadataResource(systems);

        assertEquals(expectedResult, result.getMetadataResource());
    }
	
}