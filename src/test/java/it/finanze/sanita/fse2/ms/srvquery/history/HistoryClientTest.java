package it.finanze.sanita.fse2.ms.srvquery.history;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.DELETE;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.INSERT;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.UPDATE;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.RETIRED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Date;
import java.util.Map;

import org.hl7.fhir.r4.model.BaseResource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.HistoryClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import it.finanze.sanita.fse2.ms.srvquery.history.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.history.base.TestResource;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.FhirCrudClient;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.IResBuilder;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.RSBuilder;

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
        crud.reset();
    }


    @Test
    @DisplayName("[0] empty-server")
    public void emptyServer() {
        assertEmptyServer(client.getHistoryMap(null));
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[1] null->INSERT")
    public void emptyThenAdd(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        // Check
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[2] null->INSERT+INSERT")
    public void emptyThenMultipleAdd(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        String id2 = crud.createResource(res[1].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResource(changes, res[0].name(), id2, "1", INSERT);
        assertResourceSize(2, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[3] null->INSERT+UPDATE")
    public void emptyThenAddUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        // Update it
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "2", INSERT);
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[4] null->INSERT+DELETE")
    public void emptyThenAddRemove(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Remove it
        crud.deleteResource(id, res[0].type());
        // Check
        assertEmptyServer(client.getHistoryMap(null));
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[5] null->INSERT+UPDATE+DELETE")
    public void emptyThenAddUpdateRemove(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Update it
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Remove it
        crud.deleteResource(id, res[0].type());
        // Verify again
        assertEmptyServer(client.getHistoryMap(null));
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[6] null->INSERT+UPDATE+UPDATE")
    public void emptyThenAddUpdateUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Update
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Update again
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("O", "Other");
        crud.updateResource(builder.build());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "3", INSERT);
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[7] null->INSERT->null->INSERT")
    public void emptyThenAddThenAdd(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Insert resource
        String id2 = crud.createResource(res[1].resource());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResource(changes, res[1].name(), id2, "1", INSERT);
        assertResourceSize(2, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[8] null->INSERT->null->UPDATE")
    public void emptyThenAddThenUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("O", "Other");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "2", INSERT);
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[9] null->INSERT->null->DELETE")
    public void emptyThenAddThenRemove(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Delete resource
        crud.deleteResource(id, res[0].type());
        // Check
        assertEmptyServer(client.getHistoryMap(null));
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[10] null->INSERT->null->UPDATE+DELETE")
    public void emptyThenAddThenUpdateRemove(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("O", "Other");
        crud.updateResource(builder.build());
        // Delete resource
        crud.deleteResource(id, res[0].type());
        // Check
        assertEmptyServer(client.getHistoryMap(null));
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[11] null->INSERT->null->UPDATE+UPDATE")
    public void emptyThenAddThenMultipleUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Update resource
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("O", "Other");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "3", INSERT);
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[12] null->INSERT->null->UPDATE->null->DELETE")
    public void emptyThenAddThenUpdateThenDelete(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "2", INSERT);
        assertResourceSize(1, changes);
        // Delete resource
        crud.deleteResource(id, res[0].type());
        // Check
        changes = client.getHistoryMap(null);
        assertEmptyServer(changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[13] null->INSERT->null->UPDATE->null->UPDATE")
    public void emptyThenAddThenUpdateThenUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "2", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("O", "Other");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "3", INSERT);
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[14] null->INSERT->null->UPDATE+UPDATE->null->UPDATE")
    public void emptyThenAddThenMultipleUpdateThenUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Check
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "1", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("O", "Other");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "3", INSERT);
        assertResourceSize(1, changes);
        // Update resource
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("A", "Alpha");
        crud.updateResource(builder.build());
        // Check
        changes = client.getHistoryMap(null);
        assertResource(changes, res[0].name(), id, "4", INSERT);
        assertResourceSize(1, changes);
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
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
     * Verify the flow T0->EMPTY->T1->INSERT(1)+UPDATE(2)+UPDATE(3)
     * At t0 it's expected no insertion, at t1 one insert and multiple updates
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
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
    public void resourceFromAnyToActive(TestResource res) {
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res.type()));
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
    public void resourceFromAnyToActiveSingle(TestResource res) {
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res.type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res.name(), id, "2", INSERT, "t0");
    }

    /**
     * Verify the flow t0->INSERT->t1->UPDATE, it should return an insert operation
     * because if an element has been inserted as active status, then it becomes deactivated
     * it should be treated as a deletion
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/update element status from active to draft")
    public void resourceFromActiveToAny(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Get history
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        // Verify emptiness
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // ================
        // ===== <T1> =====
        // ================
        // Get time
        now = new Date();
        // Change status from ACTIVE to DEACTIVATED
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(RETIRED);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "2", DELETE, "t1");
    }

    /**
     * Verify the flow t0->INSERT+UPDATE, it should return nothing
     * because if an element has been inserted as active status, then it becomes deactivated
     * before alignment, it should be omitted
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Last update is not null then add/update element status from draft to active")
    public void resourceFromActiveToAnySingle(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Get history
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        // Verify emptiness
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // Change status from ACTIVE to DEACTIVATED
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(RETIRED);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    /*
     * Verify the flow t0->INSERT, it should return 1 element inserted
     * Verify the flow t1->UPDATE+DELETE, it should return 1 element updated at time t1
     * Verify t2, it should return 0 element because it was deleted in t1 flow
     */
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Update and delete should return one element updated")
    void insertUpdateDeleteTest(TestResource[] res) {
    	// Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Get history
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        // Verify size 1 on fhir
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
        // ================
        // ===== <T1> =====
        // ================
        // => Update one resource, delete it and verify
        // Retrieve current time
        now = new Date();
        // Update CS
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Verify on T1
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", DELETE, "t1");
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
    
    /*
     * Verify the flow t0->INSERT+DELETE+UPDATE
     * Return only one inserted
     */
    @Disabled("Not possible using FHIR 5.7.0 execute a PUT on a DELETED resource")
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Insert, delete and update should return one element inserted")
    void insertDeleteUpdateTest(TestResource[] res) {
    	// Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Get history
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        // Verify emptiness
        assertEmptyServer(changes, "t0");
        // Update CS
        IResBuilder builder = RSBuilder.from(crud.readResource(id, "1", res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Get history
        changes = client.getHistoryMap(now);
        // Verify emptiness
        assertResource(changes, res[0].name(), id, "3", INSERT, "t1");
        assertResourceSize(1, changes);
    }

    /*
     * Verify the flow t0->INSERT+UPDATE
     * return one resource inserted
     * Verify the flow t1->DELETE+UPDATE
     * return one resource updated
     */
    @Disabled("Not possible using FHIR 5.7.0 execute a PUT on a DELETED resource")
    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("Insert, update (t0) return one inserted and delete, update (t1) return one updated")
    void insertUpdateAfterDeleteUpdateTest(TestResource[] res) {
    	// Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Update CS
        IResBuilder builder = RSBuilder.from(crud.readResource(id, "1", res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Get history
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        // Verify size 1 on fhir
        assertResource(changes, res[0].name(), id, "2", INSERT, "t0");
        assertResourceSize(1, changes);
        // ================
        // ===== <T1> =====
        // ================
        // => Update one resource, delete it and verify
        // Retrieve current time
        now = new Date();
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Update CS
        builder = RSBuilder.from(crud.readResource(id, "2", res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify on T1
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "4", UPDATE, "t1");
        assertResourceSize(1, changes);
    }

    @AfterAll
    public void teardown() {
        crud.reset();
    }

}
