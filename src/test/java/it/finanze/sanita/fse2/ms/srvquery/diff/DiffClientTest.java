package it.finanze.sanita.fse2.ms.srvquery.diff;

import it.finanze.sanita.fse2.ms.srvquery.diff.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffOpType;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.FhirCrudClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.dto.CSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.*;

import java.util.Date;
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

    /**
     * Verify an empty server doesn't return any changeset
     */
    @Test
    @DisplayName("Last update is null and no items")
    public void emptyServer() {
        Map<String, DiffOpType> changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
    }

    /**
     * Verify the flow null->INSERT->null->INSERT
     * At first changeset is expected one insertion,
     * at the second one are expected two insertions
     */
    @Test
    @DisplayName("Last update is null then add items")
    public void emptyServerThenAddMore() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        Map<String, DiffOpType> changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        changes = client.getChangesetCS(null);
        // Check
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(null)");
        assertEquals(INSERT, changes.get(gender), "Expected gender as an insert op after findByLastUpdate(null)");
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        changes = client.getChangesetCS(null);
        // Check
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(null)");
        assertTrue(changes.containsKey(ore), "Expected ore id not found after findByLastUpdate(null)");
        assertEquals(INSERT, changes.get(gender), "Expected gender as an insert op after findByLastUpdate(null)");
        assertEquals(INSERT, changes.get(ore), "Expected ore as an insert op after findByLastUpdate(null)");
        assertEquals(cs.length, changes.size(), "Expected size doesn't match current one");
    }

    /**
     * Verify the flow null->INSERT+DELETE returns an empty changeset
     * because if an element has been inserted and deleted before an alignment
     * there is no point into returning it
     */
    @Test
    @DisplayName("Last update is null then add/remove items")
    public void emptyServerThenAddRemove() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        Map<String, DiffOpType> changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        changes = client.getChangesetCS(null);
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(null)");
        assertEquals(INSERT, changes.get(gender), "Expected gender as an insert op after findByLastUpdate(null)");
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
    }

    /**
     * Verify the flow T0->INSERT->T1->INSERT->T2->NO-OP
     * does return an appropriate changeset reflecting
     * t0 as two insertions, t1 as one insertion and t2 as no changes
     * (because it's updated)
     */
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
        Map<String, DiffOpType> changes = client.getChangesetCS(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(1, changes.size(), "Expected size doesn't match");
        assertEquals(INSERT, changes.get(gender), "Expected gender as an insert op after findByLastUpdate(t0)");
        // ================
        // ===== <T1> =====
        // ================
        // => Add one resource and verify
        // Retrieve current time
        now = getCurrentTime();
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.containsKey(ore), "Expected ore id not found after findByLastUpdate(t1)");
        assertEquals(1, changes.size(), "Expected size doesn't match");
        assertEquals(INSERT, changes.get(ore), "Expected ore as an insert op after findByLastUpdate(t1)");
        // ================
        // ===== <T2> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = getCurrentTime();
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an updated server (t2)");
        // ====================
        // ===== <TO->T2> =====
        // ====================
        // => Retrieve from T0 to T2
        // Verify again
        changes = client.getChangesetCS(init);
        // Check
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertTrue(changes.containsKey(ore), "Expected ore id not found after findByLastUpdate(t0)");
        assertEquals(INSERT, changes.get(gender), "Expected gender as an insert op after findByLastUpdate(t0)");
        assertEquals(INSERT, changes.get(ore), "Expected ore as an insert op after findByLastUpdate(t0)");
        assertEquals(cs.length, changes.size(), "Expected size doesn't match current one");
    }

    /**
     * Verify the flow T0->INSERT->T1->UPDATE->T2-DELETE
     * At t0 it's expected one insertion, at t2 one update and then at t3 one delete
     */
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
        Map<String, DiffOpType> changes = client.getChangesetCS(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(1, changes.size(), "Expected size doesn't match");
        assertEquals(INSERT, changes.get(gender), "Expected gender as an insert op after findByLastUpdate(t0)");
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
        changes = client.getChangesetCS(now);
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(t1)");
        assertEquals(UPDATE, changes.get(gender), "Expected gender as an update op after findByLastUpdate(t1)");
        assertEquals(1, changes.size(), "Expected size doesn't match");
        // ================
        // ===== <T2> =====
        // ================
        // => Delete one resource and verify
        // Retrieve current time
        now = getCurrentTime();
        // Delete CS
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(t2)");
        assertEquals(1, changes.size(), "Expected size doesn't match");
        assertEquals(DELETE, changes.get(gender), "Expected gender as a delete op after findByLastUpdate(t2)");
        // ================
        // ===== <T3> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = getCurrentTime();
        // Verify again
        changes = client.getChangesetCS(now);
        // Verify emptiness
        assertTrue(changes.isEmpty(), "Expecting no ids from an updated server (t3)");
    }

    /**
     * Verify the flow t0->INSERT+DELETE returns an empty changeset
     * because if an element has been inserted and deleted before an alignment
     * there is no point into returning it
     */
    @Test
    @DisplayName("Last update is not null then add/remove item in-between the time-range")
    public void omitCreatedAndRemovedResources() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        Map<String, DiffOpType> changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Get time
        Date now = getCurrentTime();
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
    }

    /**
     * Verify the flow t0->INSERT+UPDATE returns an insert operation
     * because if an element has been inserted and updated before an alignment
     * there is no point into making a diff if it wasn't on the server in the first place
     * just treat it as an insertion
     */
    @Test
    @DisplayName("Last update is not null then add/update item in-between the time-range")
    public void resourceIsCreatedAndUpdated() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createGenderTestCS(), createOreTestCS()};
        // Verify emptiness
        Map<String, DiffOpType> changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Get time
        Date now = getCurrentTime();
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Now update it
        CSBuilder builder = CSBuilder.from(crud.readResource(gender, CodeSystem.class));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getChangesetCS(now);
        assertTrue(changes.containsKey(gender), "Expected gender id not found after findByLastUpdate(t0)");
        assertEquals(INSERT, changes.get(gender), "Expected and insert type for created and updated only files (t0)");
    }

    /**
     * Given a big resource, data should be omitted from the changeset response
     */
    @Test
    @DisplayName("Check hyper-test cs")
    public void resourceIsBig() {
        // To insert
        CodeSystem[] cs = new CodeSystem[]{createHyperTestCS()};
        // Verify emptiness
        Map<String, DiffOpType> changes = client.getChangesetCS(null);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        changes = client.getChangesetCS(null, true);
        assertTrue(changes.containsKey(gender), "Expected hyper id not found after findByLastUpdate(null)");
        assertEquals(INSERT, changes.get(gender), "Expected hyper as an insert op after findByLastUpdate(null)");
    }

    @AfterAll
    public void teardown() {
        client.resetFhir();
    }

}
