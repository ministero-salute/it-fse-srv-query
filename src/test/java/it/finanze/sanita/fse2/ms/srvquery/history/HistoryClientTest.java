package it.finanze.sanita.fse2.ms.srvquery.history;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.HistoryClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.history.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.history.base.TestResource;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.FhirCrudClient;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.IResBuilder;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.RSBuilder;
import org.hl7.fhir.r4.model.BaseResource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.*;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * TerminologyServer MUST BE set as UTC time,
 * otherwise the test suite won't work
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
class HistoryClientTest extends AbstractTestResources {

    private final HistoryClient client;
    private final FhirCrudClient crud;

    public HistoryClientTest(@Autowired HistoryClient client, @Autowired FhirCFG fhir) {
        this.client = client;
        this.crud = new FhirCrudClient(
            fhir.getFhirServerUrl(),
            fhir.getFhirServerUser(),
            fhir.getFhirServerPwd()
        );
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
        assertEmptyServer(client.getHistoryMap(null));
    }

    /**
     * Verify the flow null->INSERT->null->INSERT
     * At first changeset is expected one insertion,
     * at the second one are expected two insertions
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is null then add items")
    public void emptyServerThenAddMore(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        // Check
        assertResource(changes, res[0].name(), id, "1", INSERT);
        // Insert resource
        String id2 = crud.createResource(res[1].resource());
        // Verify again
        changes = client.getHistoryMap(null);
        // Check
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResource(changes, res[1].name(), id2, "1", INSERT);
        assertResourceSize(2, changes);
    }

    /**
     * Verify the flow null->INSERT+DELETE returns an empty changeset
     * because if an element has been inserted and deleted before an alignment
     * there is no point into returning it
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is null then add/remove items")
    public void emptyServerThenAddRemove(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Verify again
        assertEmptyServer(client.getHistoryMap(null));
    }

    /**
     * Verify the flow T0->INSERT->T1->INSERT->T2->NO-OP
     * does return an appropriate changeset reflecting
     * t0 as two insertions, t1 as one insertion and t2 as no changes
     * (because it's updated)
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add items")
    public void dateWithAddItems(TestResource[] res) {
        // ================
        // ===== <T0> =====
        // ================
        // => Check emptiness, then add one resource and verify
        // Retrieve current time
        Date init = new Date();
        Date now = init;
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // ================
        // ===== <T1> =====
        // ================
        // => Add one resource and verify
        // Retrieve current time
        now = new Date();
        // Insert resource
        String id2 = crud.createResource(res[1].resource());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[1].name(), id2, "1", INSERT, "t1");
        // ================
        // ===== <T2> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = new Date();
        // Verify again
        changes = client.getHistoryMap(now);
        assertEmptyServer(changes, "t2");
        // ====================
        // ===== <TO->T2> =====
        // ====================
        // => Retrieve from T0 to T2
        // Verify again
        changes = client.getHistoryMap(init);
        // Check
        assertResource(changes, res[0].name(), id, "1", INSERT, "t2");
        assertResource(changes, res[1].name(), id2, "1", INSERT, "t2");
        assertResourceSize(2, changes);
    }

    /**
     * Verify the flow T0->INSERT->T1->UPDATE->T2-DELETE
     * At t0 it's expected one insertion, at t1 one update and then at t2 one delete
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/remove items")
    public void dateWithAddRemoveItems(TestResource[] res) {
        // ================
        // ===== <T0> =====
        // ================
        // => Check emptiness, then add one resource and verify
        // Retrieve current time
        Date now = new Date();
        // Verify emptiness
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // ================
        // ===== <T1> =====
        // ================
        // => Update one resource and verify
        // Retrieve current time
        now = new Date();
        // Update CS
        IResBuilder<?> builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "2", UPDATE, "t1");
        assertResourceSize(1, changes);
        // ================
        // ===== <T2> =====
        // ================
        // => Delete one resource and verify
        // Retrieve current time
        now = new Date();
        // Delete CS
        crud.deleteResource(id, res[0].type());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", DELETE, "t2");
        assertResourceSize(1, changes);
        // ================
        // ===== <T3> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = new Date();
        // Verify again
        changes = client.getHistoryMap(now);
        // Verify emptiness
        assertEmptyServer(changes, "t3");
    }

    /**
     * Verify the flow T0->INSERT(1)->T1->UPDATE(2)+UPDATE(3)+UPDATE(4)->T2-DELETE(4)
     * At t0 it's expected one insertion, at t1 multiple updates and then at t2 one delete
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/update/delete items")
    public void dateWithUpdatedThenDeleteItems(TestResource[] res) {
        // ================
        // ===== <T0> =====
        // ================
        // => Check emptiness, then add one resource and verify
        // Retrieve current time
        Date now = new Date();
        // Verify emptiness
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // ================
        // ===== <T1> =====
        // ================
        // => Update one resource and verify
        // Retrieve current time
        now = new Date();
        // Update CS
        IResBuilder<?> builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("T", "Test");
        crud.updateResource(builder.build());
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("X", "XTest");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "4", UPDATE, "t1");
        assertResourceSize(1, changes);
        // ================
        // ===== <T2> =====
        // ================
        // => Delete one resource and verify
        // Retrieve current time
        now = new Date();
        // Delete CS
        crud.deleteResource(id, res[0].type());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "5", DELETE, "t2");
        assertResourceSize(1, changes);
        // ================
        // ===== <T3> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = new Date();
        // Verify again
        changes = client.getHistoryMap(now);
        // Verify emptiness
        assertEmptyServer(changes, "t3");
    }

    /**
     * Verify the flow T0->INSERT(1)+UPDATE(2)+UPDATE(3)->T1->EMPTY
     * At t0 it's expected one insertion, at t1 one insert and multiple updates
     * and then at t2 one must be synchronised
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/update items")
    public void dateWithUpdatedItems(TestResource[] res) {
        // ================
        // ===== <T0> =====
        // ================
        // => Check emptiness, then add one resource and verify
        // Retrieve current time
        Date now = new Date();
        // Verify emptiness
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
        // ================
        // ===== <T1> =====
        // ================
        // Retrieve current time
        now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // Update CS
        IResBuilder<?> builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("T", "Test");
        crud.updateResource(builder.build());
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("X", "XTest");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "4", INSERT, "t1");
        assertResourceSize(1, changes);
        // ================
        // ===== <T2> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = new Date();
        // Verify again
        changes = client.getHistoryMap(now);
        // Verify emptiness
        assertEmptyServer(changes, "t2");
    }

    /**
     * Verify the flow t0->INSERT+DELETE returns an empty changeset
     * because if an element has been inserted and deleted before an alignment
     * there is no point into returning it
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/remove item in-between the time-range")
    public void omitCreatedAndRemovedResources(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Verify again
        assertEmptyServer(client.getHistoryMap(now), "t0");
    }

    /**
     * Verify the flow t0->INSERT+UPDATE returns an insert operation
     * because if an element has been inserted and updated before an alignment
     * there is no point into making a diff if it wasn't on the server in the first place
     * just treat it as an insertion
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/update item in-between the time-range")
    public void resourceIsCreatedAndUpdated(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Now update it
        IResBuilder<?> builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "2", INSERT, "t0");
    }

    /**
     * Verify the flow t0->INSERT->t1->UPDATE, it should return an insert operation
     * because if an element has been inserted as whatever status, then it becomes active
     * it should be treated as an insertion
     */
    @ParameterizedTest
    @MethodSource("getTestResourcesDraft")
    @DisplayName("Last update is not null then add/update element status from draft to active")
    public void resourceFromDraftToActive(TestResource res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res.resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // ================
        // ===== <T1> =====
        // ================
        // Get time
        now = new Date();
        // Change status from DRAFT to ACTIVE
        IResBuilder<?> builder = RSBuilder.from(crud.readResource(id, res.type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res.name(), id, "2", INSERT, "t1");
    }

    /**
     * Verify the flow t0->INSERT+UPDATE, it should return an insert operation
     * because if an element has been inserted as whatever status, then it becomes active
     * it should be treated as an insertion
     */
    @ParameterizedTest
    @MethodSource("getTestResourcesDraft")
    @DisplayName("Last update is not null then add/update element status from draft to active")
    public void resourceFromDraftToActiveSingle(TestResource res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res.resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // Change status from DRAFT to ACTIVE
        IResBuilder<?> builder = RSBuilder.from(crud.readResource(id, res.type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res.name(), id, "2", INSERT, "t0");
    }

    @AfterAll
    public void teardown() {
        client.resetFhir();
    }

}
