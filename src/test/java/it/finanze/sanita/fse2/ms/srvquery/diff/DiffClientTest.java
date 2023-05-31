package it.finanze.sanita.fse2.ms.srvquery.diff;

import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.FhirCrudClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.dto.CSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class DiffClientTest {

    private static final String BASE_URL = "http://localhost:8080/fhir/";

    private final DiffClient client;
    private final FhirCrudClient crud;

    public DiffClientTest() {
        this.client = new DiffClient(BASE_URL, "admin", "admin");
        this.crud = new FhirCrudClient(BASE_URL, "admin", "admin");
    }

    @BeforeEach
    public void init() {
        client.resetFhir();
    }

    @Test
    @DisplayName("Check last updated items with a null date and an empty server")
    public void emptyServer() {
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
    }

    @Test
    @DisplayName("Check last updated items with a null date, an empty server then add several items")
    public void emptyServerThenAddOne() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        emptyServer();
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class);
        // Check
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(null)");
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        ids = client.findByLastUpdate(null, CodeSystem.class);
        // Check
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(null)");
        assertTrue(ids.contains(ore), "Expected ore id not found after findByLastUpdate(null)");
        assertEquals(cs.length, ids.size(), "Expected size doesn't match current one");
    }

    @AfterAll
    public void teardown() {
        client.resetFhir();
    }

    private CodeSystem createGenderTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.5.1");
        builder.addCodes("M", "Male");
        builder.addCodes("F", "Female");
        return builder.build();
    }

    private CodeSystem createOreTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.2.9.6.1.54.6");
        builder.addCodes("P", "Platinum");
        builder.addCodes("D", "Diamond");
        builder.addCodes("G", "Gold");
        return builder.build();
    }

}
