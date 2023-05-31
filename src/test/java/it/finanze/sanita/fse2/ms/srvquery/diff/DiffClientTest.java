package it.finanze.sanita.fse2.ms.srvquery.diff;

import it.finanze.sanita.fse2.ms.srvquery.diff.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.FhirCrudClient;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;

import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class DiffClientTest extends AbstractTestResources {

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
    @DisplayName("Last update is null and no items")
    public void emptyServer() {
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
    }

    @Test
    @DisplayName("Last update is null then add items")
    public void emptyServerThenAddMore() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(null, CodeSystem.class);
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

    @Test
    @DisplayName("Last update is null then add/remove items")
    public void emptyServerThenAddRemove() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(null, CodeSystem.class);
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(null)");
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        ids = client.findByLastUpdate(null, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
    }

    @Test
    @DisplayName("Last update is not null then add items")
    public void dateWithAddItems() {
        // ================
        // ===== <T0> =====
        // ================
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Retrieve current time
        Date init = getCurrentTime();
        Date now = init;
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(now, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class);
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T1> =====
        // ================
        // Retrieve current time
        now = getCurrentTime();
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class);
        assertTrue(ids.contains(ore), "Expected ore id not found after findByLastUpdate(t1)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T0> =====
        // ================
        // Verify again
        ids = client.findByLastUpdate(init, CodeSystem.class);
        // Check
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertTrue(ids.contains(ore), "Expected ore id not found after findByLastUpdate(t0)");
        assertEquals(cs.length, ids.size(), "Expected size doesn't match current one");
    }

    @Test
    @DisplayName("Last update is not null then add/remove items")
    public void dateWithAddRemoveItems() {
        // ================
        // ===== <T0> =====
        // ================
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Retrieve current time
        Date now = getCurrentTime();
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(now, CodeSystem.class);
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class);
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T1> =====
        // ================
        // Retrieve current time
        now = getCurrentTime();
        // Delete CS
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class);
        assertTrue(ids.contains(gender), "Expected gender id found after findByLastUpdate(t1)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
    }

    @AfterAll
    public void teardown() {
        client.resetFhir();
    }

}
