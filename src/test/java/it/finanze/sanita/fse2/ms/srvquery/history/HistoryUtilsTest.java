package it.finanze.sanita.fse2.ms.srvquery.history;

import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.history.base.AbstractTestResources;
import it.finanze.sanita.fse2.ms.srvquery.history.base.TestResource;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.FhirCrudClient;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * TerminologyServer MUST BE set as UTC time,
 * otherwise the test suite won't work
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
@Slf4j
public class HistoryUtilsTest extends AbstractTestResources {

    private final FhirCrudClient crud;

    public HistoryUtilsTest(@Autowired FhirCFG fhir) {
        this.crud = new FhirCrudClient(
            fhir.getFhirServerUrl(),
            fhir.getFhirServerUser(),
            fhir.getFhirServerPwd()
        );
    }

    @Test
    void insertResources() {
        // Retrieve test resources
        for (TestResource res : getTestResourcesMixed()) {
            String id = crud.createResource(res.resource());
            log.info("Insert {} as {} with id {}", res.name(), res.type().getSimpleName(), id);
        }
    }

    @Test
    void insertWhitelistResources() {
        CodeSystem cs = (CodeSystem) createOreTestCS();
        cs.setConcept(new ArrayList<>());
        TestResource ore = new TestResource("ore", cs);
        String id = crud.createResource(cs);
        log.info("Insert {} as {} with id {}", ore.name(), ore.type().getSimpleName(), id);
    }

    @Test
    void reset() {
        crud.reset();
    }

}

