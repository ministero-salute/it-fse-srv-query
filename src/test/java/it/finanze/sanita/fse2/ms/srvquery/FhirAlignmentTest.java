package it.finanze.sanita.fse2.ms.srvquery;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.client.impl.CSDiffCalculator.OP_ADD;
import static it.finanze.sanita.fse2.ms.srvquery.client.impl.CSDiffCalculator.createChangeset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FhirAlignmentTest {

    private static final String BASE_URL = "http://localhost:8080/fhir/";
    private final FHIRClient fhir;
    private final IGenericClient client;

    public FhirAlignmentTest() {
        this.fhir = new FHIRClient(BASE_URL, "admin", "admin");
        this.client = FHIRR4Helper.createClient(BASE_URL, "admin", "admin");
    }

    @BeforeEach
    public void init() {
        // Reset FHIR for CodeSystem
        client
        .operation()
        .onServer()
        .named("$expunge")
        .withParameter(
            Parameters.class, "expungeEverything", new BooleanType(true)
        ).execute();
    }

    @Test
    public void lastUpdateIsNull() {
        Pair<Date, Map<String, Map<String, List<String>>>> changeset = createChangeset(fhir, client, null);
        // There are NO code systems available
        assertTrue(changeset.getValue().isEmpty());
        // Now register some test codes
        List<CodeDTO> codes = new ArrayList<>();
        codes.add(new CodeDTO("A", "Letter A", null));
        codes.add(new CodeDTO("B", "Letter B", null));
        codes.add(new CodeDTO("C", "Letter C", null));
        // Insert
        String c0 = fhir.insertCS("code-test-0", codes);
        // Now register some test codes
        codes = new ArrayList<>();
        codes.add(new CodeDTO("D", "Letter D", null));
        codes.add(new CodeDTO("E", "Letter E", null));
        codes.add(new CodeDTO("F", "Letter F", null));
        // Insert
        String c1 = fhir.insertCS("code-test-1", codes);
        changeset = createChangeset(fhir, client, null);
        // There are NO code systems available
        Map<String, Map<String, List<String>>> map = changeset.getValue();
        assertEquals(map.get(c0).get(OP_ADD).size(), 3);
        assertEquals(map.get(c1).get(OP_ADD).size(), 3);
    }

    @Test
    public void startsEmptyThenAddOne() {
        Pair<Date, Map<String, Map<String, List<String>>>> changeset = createChangeset(fhir, client, null);
        // There are NO code systems available
        assertTrue(changeset.getValue().isEmpty());
        // Now register some test codes
        List<CodeDTO> codes = new ArrayList<>();
        codes.add(new CodeDTO("A", "Letter A", null));
        codes.add(new CodeDTO("B", "Letter B", null));
        codes.add(new CodeDTO("C", "Letter C", null));
        // Insert
        String c0 = fhir.insertCS("code-test-0", codes);
        // Now register some test codes
        codes = new ArrayList<>();
        codes.add(new CodeDTO("D", "Letter D", null));
        codes.add(new CodeDTO("E", "Letter E", null));
        codes.add(new CodeDTO("F", "Letter F", null));
        // Insert
        String c1 = fhir.insertCS("code-test-1", codes);
        changeset = createChangeset(fhir, client, null);
        // There are NO code systems available
        Map<String, Map<String, List<String>>> map = changeset.getValue();
        assertEquals(map.get(c0).get(OP_ADD).size(), 3);
        assertEquals(map.get(c1).get(OP_ADD).size(), 3);
    }

}
