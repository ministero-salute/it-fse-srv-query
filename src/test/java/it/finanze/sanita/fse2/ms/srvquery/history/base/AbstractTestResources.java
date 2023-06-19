package it.finanze.sanita.fse2.ms.srvquery.history.base;

import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.CSBuilder;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.VSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ValueSet;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractTestResources {
    protected CodeSystem createGenderTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.5.1");
        builder.addCodes("M", "Male");
        builder.addCodes("F", "Female");
        return builder.build();
    }

    protected CodeSystem createOreTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.2.9.6.1.54.6");
        builder.addCodes("P", "Platinum");
        builder.addCodes("D", "Diamond");
        builder.addCodes("G", "Gold");
        return builder.build();
    }

    protected ValueSet createColorsTestVS() {
        VSBuilder builder = new VSBuilder(
            "2.144.33.22.04.38",
            "http://url/colors"
        );
        builder.addInclusionCS("http://url/primary-colors");
        builder.addInclusionCS("http://url/secondary-colors");
        return builder.build();
    }

    protected CodeSystem createPrimaryColorsTestCS() {
        CSBuilder builder = new CSBuilder("2.144.33.22.04.39");
        builder.addUrl("http://url/primary-colors");
        builder.addCodes("R", "Red");
        builder.addCodes("Y", "Yellow");
        builder.addCodes("B", "Blue");
        return builder.build();
    }

    protected ValueSet createPrimaryColorsTestVS() {
        VSBuilder builder = new VSBuilder(
            "2.144.33.22.04.39",
            "http://url/primary-colors"
        );
        builder.addCodes("R", "Red");
        builder.addCodes("Y", "Yellow");
        builder.addCodes("B", "Blue");
        return builder.build();
    }

    protected CodeSystem createSecondaryColorsTestCS() {
        CSBuilder builder = new CSBuilder("2.144.33.22.04.40");
        builder.addUrl("http://url/secondary-colors");
        builder.addCodes("O", "Orange");
        builder.addCodes("G", "Green");
        builder.addCodes("V", "Violet");
        return builder.build();
    }

    protected ValueSet createSecondaryColorsTestVS() {
        VSBuilder builder = new VSBuilder(
            "2.144.33.22.04.40",
            "http://url/secondary-colors"
        );
        builder.addCodes("O", "Orange");
        builder.addCodes("G", "Green");
        builder.addCodes("V", "Violet");
        return builder.build();
    }

    protected void assertEmptyServer(Map<String, HistoryDetailsDTO> changes) {
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
    }

    protected void assertEmptyServer(Map<String, HistoryDetailsDTO> changes, String delta) {
        assertTrue(changes.isEmpty(), "Expecting no ids from server at " + delta);
    }

    protected void assertResource(
        Map<String, HistoryDetailsDTO> changes,
        String name,
        String id,
        String version,
        HistoryOperationEnum op
    ) {
       assertResource(changes, name, id, version, op, "lastUpdate=null");
    }

    protected void assertResource(
        Map<String, HistoryDetailsDTO> changes,
        String name,
        String id,
        String version,
        HistoryOperationEnum op,
        String delta
    ) {
        assertTrue(changes.containsKey(id), format("Expected %s id not found after %s", name, delta));
        assertEquals(op, changes.get(id).getOp(), format("Expected %s as an %s op after %s", name, op.name(), delta));
        assertEquals(version, changes.get(id).getVersion(), format("Expected %s version doesn't match", name));
    }

    protected void assertResourceSize(int size, Map<String, HistoryDetailsDTO> changes) {
        assertEquals(size, changes.size(), "Expected size doesn't match current one");
    }

    @SuppressWarnings("unused")
    protected String printDateAsUTC(Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), UTC).toString();
    }

}
