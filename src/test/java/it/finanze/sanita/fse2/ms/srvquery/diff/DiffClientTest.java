package it.finanze.sanita.fse2.ms.srvquery.diff;

import it.finanze.sanita.fse2.ms.srvquery.diff.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffOpType;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffResult;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.FhirCrudClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.dto.CSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffOpType.*;
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
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
    }

    @Test
    @DisplayName("Last update is null then add items")
    public void emptyServerThenAddMore() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        DiffResult res;
        List<String> ids;
        Map<String, DiffOpType> map;
        ids = client.findByLastUpdate(null, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        res = client.findByLastUpdate(null, CodeSystem.class);
        ids = res.ids();
        map = res.mapping();
        // Check
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(null)");
        assertEquals(map.get(gender), INSERT, "Expected gender as an insert op after findByLastUpdate(null)");
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        res = client.findByLastUpdate(null, CodeSystem.class);
        ids = res.ids();
        map = res.mapping();
        // Check
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(null)");
        assertTrue(ids.contains(ore), "Expected ore id not found after findByLastUpdate(null)");
        assertEquals(map.get(gender), INSERT, "Expected gender as an insert op after findByLastUpdate(null)");
        assertEquals(map.get(ore), INSERT, "Expected ore as an insert op after findByLastUpdate(null)");
        assertEquals(cs.length, ids.size(), "Expected size doesn't match current one");
    }

    @Test
    @DisplayName("Last update is null then add/remove items")
    public void emptyServerThenAddRemove() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(null, CodeSystem.class).ids();
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(null)");
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        ids = client.findByLastUpdate(null, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
    }

    @Test
    @DisplayName("Last update is not null then add items")
    public void dateWithAddItems() {
        // ================
        // ===== <T0> =====
        // ================
        // => Check emptiness, then add one resource and verify
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Retrieve current time
        Date init = getCurrentTime();
        Date now = init;
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T1> =====
        // ================
        // => Add one resource and verify
        // Retrieve current time
        now = getCurrentTime();
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.contains(ore), "Expected ore id not found after findByLastUpdate(t1)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T2> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = getCurrentTime();
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an updated server (t2)");
        // ====================
        // ===== <TO->T2> =====
        // ====================
        // => Retrieve from T0 to T2
        // Verify again
        ids = client.findByLastUpdate(init, CodeSystem.class).ids();
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
        // => Check emptiness, then add one resource and verify
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Retrieve current time
        Date now = getCurrentTime();
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T1> =====
        // ================
        // => Update one resource and verify
        // Retrieve current time
        now = getCurrentTime();
        // Update CS
        CSBuilder builder = CSBuilder.from(crud.readResource(gender, CodeSystem.class));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t1)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T2> =====
        // ================
        // => Delete one resource and verify
        // Retrieve current time
        now = getCurrentTime();
        // Delete CS
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.contains(gender), "Expected gender id not found after findByLastUpdate(t2)");
        assertEquals(1, ids.size(), "Expected size doesn't match");
        // ================
        // ===== <T3> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = getCurrentTime();
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        // Verify emptiness
        assertTrue(ids.isEmpty(), "Expecting no ids from an updated server (t3)");
    }

    @Test
    @DisplayName("Last update is not null then add/remove items in-between the time-range")
    public void omitCreatedAndRemovedResources() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        List<String> ids = client.findByLastUpdate(null, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
        // Get time
        Date now = getCurrentTime();
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        ids = client.findByLastUpdate(now, CodeSystem.class).ids();
        assertTrue(ids.isEmpty(), "Expecting no ids from an empty server");
    }

    @AfterAll
    public void teardown() {
        client.resetFhir();
    }

}
