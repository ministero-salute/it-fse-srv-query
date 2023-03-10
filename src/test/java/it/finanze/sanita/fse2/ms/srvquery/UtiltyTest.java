package it.finanze.sanita.fse2.ms.srvquery;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility.getSearchParamFromMasterId;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
class UtiltyTest {
    
    @Test
    void searchParamsTest() {
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId(null));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId(""));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^ab"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("ab^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("ab^^cd"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  ^  "));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^  "));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  ^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  "));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("^^^^"));
        assertThrows(IllegalArgumentException.class, () -> getSearchParamFromMasterId("  ^^  ^^"));
        assertEquals("cd", getSearchParamFromMasterId("ab^cd"));
        assertEquals("abcd", getSearchParamFromMasterId("abcd"));
        assertEquals("UAT_GTW_ID162", getSearchParamFromMasterId("2.16.840.4^UAT_GTW_ID162"));
    }

    @Test
    void serializationTest() {

        byte[] jsonFhir = FileUtility.getFileFromInternalResources("Files/CreationJsonFhir.json");
        String json = new String(jsonFhir, StandardCharsets.UTF_8);
        Object obj = FHIRR4Helper.deserializeResource(Bundle.class, json, true);

        assertDoesNotThrow(() -> FHIRR4Helper.serializeResource((Bundle) obj, true, true, true));
    }
}
