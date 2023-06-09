package it.finanze.sanita.fse2.ms.srvquery.diff;

import it.finanze.sanita.fse2.ms.srvquery.diff.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffResource;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.FhirCrudClient;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.dto.CSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffOpType.*;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffResource.NO_VERSION;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffUtils.getCurrentTime;
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
        assertEmptyServer(client.getChangesetCS(null));
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
        assertEmptyServer(client.getChangesetCS(null));
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        Map<String, DiffResource> changes = client.getChangesetCS(null);
        // Check
        assertResource(changes, "gender", gender, "1", INSERT);
        // Insert CS
        String ore = crud.createResource(cs[1]);
        // Verify again
        changes = client.getChangesetCS(null);
        // Check
        assertResource(changes, "gender", gender, "1", INSERT);
        assertResource(changes, "ore", ore, "1", INSERT);
        assertResourceSize(cs.length, changes);
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
        assertEmptyServer(client.getChangesetCS(null));
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        Map<String, DiffResource> changes = client.getChangesetCS(null);
        assertResource(changes, "gender", gender, "1", INSERT);
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        assertEmptyServer(client.getChangesetCS(null));
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
        assertEmptyServer(client.getChangesetCS(now));
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        Map<String, DiffResource> changes = client.getChangesetCS(now);
        assertResource(changes, "gender", gender, "1", INSERT, "t0");
        assertResourceSize(1, changes);
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
        assertResource(changes, "ore", ore, "1", INSERT, "t1");
        // ================
        // ===== <T2> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = getCurrentTime();
        // Verify again
        changes = client.getChangesetCS(now);
        assertEmptyServer(changes, "t2");
        // ====================
        // ===== <TO->T2> =====
        // ====================
        // => Retrieve from T0 to T2
        // Verify again
        changes = client.getChangesetCS(init);
        // Check
        assertResource(changes, "gender", gender, "1", INSERT, "t2");
        assertResource(changes, "ore", ore, "1", INSERT, "t2");
        assertResourceSize(2, changes);
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
        Map<String, DiffResource> changes = client.getChangesetCS(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Verify again
        changes = client.getChangesetCS(now);
        assertResource(changes, "gender", gender, "1", INSERT, "t0");
        assertResourceSize(1, changes);
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
        assertResource(changes, "gender", gender, "2", UPDATE, "t1");
        assertResourceSize(1, changes);
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
        assertResource(changes, "gender", gender, NO_VERSION, DELETE, "t2");
        assertResourceSize(1, changes);
        // ================
        // ===== <T3> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = getCurrentTime();
        // Verify again
        changes = client.getChangesetCS(now);
        // Verify emptiness
        assertEmptyServer(changes, "t3");
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
        assertEmptyServer(client.getChangesetCS(null));
        // Get time
        Date now = getCurrentTime();
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Now remove it
        crud.deleteResource(gender, CodeSystem.class);
        // Verify again
        assertEmptyServer(client.getChangesetCS(now), "t0");
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
        assertEmptyServer(client.getChangesetCS(null));
        // Get time
        Date now = getCurrentTime();
        // Insert CS
        String gender = crud.createResource(cs[0]);
        // Now update it
        CSBuilder builder = CSBuilder.from(crud.readResource(gender, CodeSystem.class));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        Map<String, DiffResource> changes = client.getChangesetCS(now);
        assertResource(changes, "gender", gender, "2", INSERT, "t0");
    }

    @AfterAll
    public void teardown() {
        client.resetFhir();
    }

}
