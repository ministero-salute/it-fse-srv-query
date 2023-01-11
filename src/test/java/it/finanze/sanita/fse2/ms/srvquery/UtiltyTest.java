package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
class UtiltyTest {
    
    @Test
    void searchParamsTest() {
        String searchParam = StringUtility.getSearchParameterFromMasterIdentifier("masterId^searchParam");
        assertEquals("searchParam", searchParam);

        searchParam = StringUtility.getSearchParameterFromMasterIdentifier("masterIdsearchParam");
        assertEquals("masterIdsearchParam", searchParam);
    }

    @Test
    void serializationTest() {

        byte[] jsonFhir = FileUtility.getFileFromInternalResources("Files/CreationJsonFhir.json");
        String json = new String(jsonFhir, StandardCharsets.UTF_8);
        Object obj = FHIRR4Helper.deserializeResource(Bundle.class, json, true);

        assertDoesNotThrow(() -> FHIRR4Helper.serializeResource((Bundle) obj, true, true, true));
    }
}
