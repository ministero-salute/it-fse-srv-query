package it.finanze.sanita.fse2.ms.srvquery.history;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.DELETE;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.INSERT;
import static it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum.UPDATE;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.ACTIVE;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.DRAFT;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Date;
import java.util.Map;

import org.hl7.fhir.r4.model.BaseResource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(0)
    @DisplayName("[0] empty-server")
    public void emptyServer() {
        assertEmptyServer(client.getHistoryMap(null));
    }

    @ParameterizedTest
    @Order(1)
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
    @Order(2)
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
    @Order(3)
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
    @Order(4)
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
    @Order(5)
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
    @Order(6)
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
    @Order(7)
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
    @Order(8)
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
    @Order(9)
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
    @Order(10)
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
    @Order(11)
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
    @Order(12)
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
    @Order(13)
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
    @Order(14)
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
     * Verify an empty server t0 doesn't return any changeset
     */
    @Test
    @Order(15)
    @DisplayName("[15] empty-server")
    void emptyServerT0() {
        assertEmptyServer(client.getHistoryMap(new Date()));
    }
    
    @ParameterizedTest
    @Order(16)
    @MethodSource("getTestResources")
    @DisplayName("[16] t0->INSERT")
    void resourceIsCreated(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "1", INSERT, "t0");
        assertResourceSize(1, changes);
    }
    
    @ParameterizedTest
    @Order(17)
    @MethodSource("getTestResources")
    @DisplayName("[17] t0->INSERT+INSERT")
    void twoResourcesAreCreated(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // Get time
        Date now = new Date();
        // Insert resource 1
        String id1 = crud.createResource(res[0].resource());
        // Insert resource 2
        String id2 = crud.createResource(res[1].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id1, "1", INSERT, "t0");
        assertResource(changes, res[0].name(), id2, "1", INSERT, "t0");
        assertResourceSize(2, changes);
    }
    
    @ParameterizedTest
    @Order(18)
    @MethodSource("getTestResources")
    @DisplayName("[18] t0->INSERT+UPDATE")
    void resourceIsCreatedAndUpdated(TestResource[] res) {
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
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @Order(19)
    @MethodSource("getTestResources")
    @DisplayName("[19] t0->INSERT+DELETE")
    void omitCreatedAndRemovedResources(TestResource[] res) {
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
    
    @ParameterizedTest
    @Order(20)
    @MethodSource("getTestResources")
    @DisplayName("[20] t0->INSERT+UPDATE+DELETE")
    void dateWithUpdatedDeletedItem(TestResource[] res) {
    	// Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
    	// ================
        // ===== <T0> =====
        // ================
        // Retrieve current time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Update CS
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Verify on T0
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertEmptyServer(changes, "t0");
    }
    
    @ParameterizedTest
    @Order(21)
    @MethodSource("getTestResources")
    @DisplayName("[21] t0->INSERT+UPDATE+UPDATE")
    void dateWithUpdatedItems(TestResource[] res) {
    	// Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
    	// ================
        // ===== <T0> =====
        // ================
        // Retrieve current time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Update CS
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("T", "Test");
        crud.updateResource(builder.build());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", INSERT, "t0");
        // ================
        // ===== <T1> =====
        // ================
        // => Given an updated server, verify no ids returns
        // Retrieve current time
        now = new Date();
        // Verify again
        changes = client.getHistoryMap(now);
        // Verify emptiness
        assertEmptyServer(changes, "t1");
    }

    @ParameterizedTest
    @Order(22)
    @MethodSource("getTestResources")
    @DisplayName("[22] t0->INSERT->t1->INSERT")
    void dateWithAddItems(TestResource[] res) {
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
    
    @ParameterizedTest
    @Order(23)
    @MethodSource("getTestResources")
    @DisplayName("[23] t0->INSERT->t1->UPDATE")
    void dateWithInsertAndUpdatedItem(TestResource[] res) {
    	// Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
    	// ================
        // ===== <T0> =====
        // ================
        // Retrieve current time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
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
    }
    
    @ParameterizedTest
    @Order(24)
    @MethodSource("getTestResources")
    @DisplayName("[24] t0->INSERT->t1->DELETE")
    void dateWithInsertThenDelete(TestResource[] res) {
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
        // Retrieve current time
        now = new Date();
        // Now remove the resource
        crud.deleteResource(id, res[0].type());
        // Get history
        changes = client.getHistoryMap(now);
        // Verify again
        assertResource(changes, res[0].name(), id, "2", DELETE, "t1");
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
    
    @ParameterizedTest
    @Order(25)
    @MethodSource("getTestResources")
    @DisplayName("[25] t0->INSERT->t1->UPDATE+DELETE")
    void insertUpdateDelete(TestResource[] res) {
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
    
    @ParameterizedTest
    @Order(26)
    @MethodSource("getTestResources")
    @DisplayName("[26] t0->INSERT->t1->UPDATE+UPDATE")
    void dateWithUpdatedTwoTimesItem(TestResource[] res) {
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
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", UPDATE, "t1");
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

    @ParameterizedTest
    @Order(27)
    @MethodSource("getTestResources")
    @DisplayName("[27] t0->INSERT->t1->UPDATE->t2->DELETE")
    void dateWithAddRemoveItems(TestResource[] res) {
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
    
    @ParameterizedTest
    @Order(28)
    @MethodSource("getTestResources")
    @DisplayName("[28] t0->INSERT->t1->UPDATE->t2->UPDATE")
    void insertThenUpdateTwoTimes(TestResource[] res) {
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
        // => Update resource and verify
        // Retrieve current time
        now = new Date();
        // Update CS
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("T", "Test");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", UPDATE, "t2");
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
    
    @ParameterizedTest
    @Order(29)
    @MethodSource("getTestResources")
    @DisplayName("[29] t0->INSERT->t1->UPDATE+UPDATE->t2->UPDATE")
    void insertUpdateTwoTimesThenUpdate(TestResource[] res) {
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
        // Update CS
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("T", "Test");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", UPDATE, "t1");
        assertResourceSize(1, changes);
        // ================
        // ===== <T2> =====
        // ================
        // => Delete one resource and verify
        // Retrieve current time
        now = new Date();
        // Update CS
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("X", "XTest");
        crud.updateResource(builder.build());
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "4", UPDATE, "t2");
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

    @ParameterizedTest
    @Order(30)
    @MethodSource("getTestResources")
    @DisplayName("[30] t0->INSERT->t1->UPDATE+UPDATE->t2->DELETE")
    void dateWithUpdatedThenDeleteItems(TestResource[] res) {
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
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", UPDATE, "t1");
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
        assertResource(changes, res[0].name(), id, "4", DELETE, "t2");
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
    
    @ParameterizedTest
    @Order(31)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[31] t0->INSERT(draft)")
    void insertDraftResource(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        crud.createResource(res[0].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(32)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[32] t0->INSERT(draft)+INSERT(draft)")
    void insertTwDraftResources(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource 1
        crud.createResource(res[0].resource());
        // Insert resource 2
        crud.createResource(res[1].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(33)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[33] t0->INSERT(draft)+UPDATE(active)")
    void resourceFromAnyToActiveSingle(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // Change status from DRAFT to ACTIVE
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "2", INSERT, "t0");
    }
    
    @ParameterizedTest
    @Order(34)
    @MethodSource("getTestResources")
    @DisplayName("[34] t0->INSERT(active)+UPDATE(draft)")
    void resourceFromActiveToAnySingle(TestResource[] res) {
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
        builder.addStatus(DRAFT);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(35)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[35] t0->INSERT(draft)+UPDATE(draft)")
    void resourceUpdateDraft(TestResource[] res) {
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
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(36)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[36] t0->INSERT(draft)+DELETE(draft)")
    void resourceInsertDraftDelete(TestResource[] res) {
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
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(37)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[37] t0->INSERT(draft)+UPDATE(active)+DELETE")
    void insertDraftUpdateActiveDelete(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // Change status from DRAFT to ACTIVE
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(38)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[38] t0->INSERT(draft)+UPDATE(active)+UPDATE(active)")
    void insertDraftUpdateUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // Change status from DRAFT to ACTIVE
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Update resource
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "3", INSERT, "t0");
    }
    
    @ParameterizedTest
    @Order(39)
    @MethodSource("getTestResources")
    @DisplayName("[39] t0->INSERT(active)+UPDATE(draft)+DELETE")
    void insertActiveUpdateDraftDelete(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Change status from ACTIVE to DRAFT
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(DRAFT);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Now remove it
        crud.deleteResource(id, res[0].type());
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(40)
    @MethodSource("getTestResources")
    @DisplayName("[40] t0->INSERT(active)+UPDATE(draft)+UPDATE(draft)")
    void insertActiveUpdateDraftUpdate(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Change status from ACTIVE to DRAFT
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(DRAFT);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Update resource
        builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addCodes("U", "Unknown");
        crud.updateResource(builder.build());
        // Verify again
        assertEmptyServer(client.getHistoryMap(now));
    }
    
    @ParameterizedTest
    @Order(41)
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[41] t0->INSERT(draft)->t1->INSERT(active)")
    void insertDraftInsertActiveResource(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        crud.createResource(res[0].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // ================
        // ===== <T1> =====
        // ================
        // Get time
        now = new Date();
        // Insert resource
        String id = crud.createResource(createOreTestCS());
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, "ore", id, "1", INSERT, "t1");
        assertResourceSize(1, changes);
    }

    @ParameterizedTest
    @MethodSource("getTestResources")
    @DisplayName("[42] t0->INSERT(active)->t1->UPDATE(draft)")
    void resourceFromActiveToAny(TestResource[] res) {
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
        builder.addStatus(DRAFT);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "2", DELETE, "t1");
    }
    
    @ParameterizedTest
    @MethodSource("getTestResourcesDraft")
    @DisplayName("[43] t0->INSERT(draft)->t1->UPDATE(active)")
    void resourceFromAnyToActive(TestResource[] res) {
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(null));
        // ================
        // ===== <T0> =====
        // ================
        // Get time
        Date now = new Date();
        // Insert resource
        String id = crud.createResource(res[0].resource());
        // Verify emptiness
        assertEmptyServer(client.getHistoryMap(now));
        // ================
        // ===== <T1> =====
        // ================
        // Get time
        now = new Date();
        // Change status from DRAFT to ACTIVE
        IResBuilder builder = RSBuilder.from(crud.readResource(id, res[0].type()));
        builder.addStatus(ACTIVE);
        BaseResource out = builder.build();
        // Update CS
        crud.updateResource(out);
        // Verify again
        Map<String, HistoryDetailsDTO> changes = client.getHistoryMap(now);
        assertResource(changes, res[0].name(), id, "2", INSERT, "t1");
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
