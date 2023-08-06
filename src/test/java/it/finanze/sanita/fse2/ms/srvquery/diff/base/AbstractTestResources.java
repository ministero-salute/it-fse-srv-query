package it.finanze.sanita.fse2.ms.srvquery.diff.base;

import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffOpType;
import it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffResource;
import it.finanze.sanita.fse2.ms.srvquery.diff.crud.dto.CSBuilder;
import org.hl7.fhir.r4.model.CodeSystem;

import java.util.Map;

import static java.lang.String.format;
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

    protected void assertEmptyServer(Map<String, DiffResource> changes) {
        assertTrue(changes.isEmpty(), "Expecting no ids from an empty server");
    }

    protected void assertEmptyServer(Map<String, DiffResource> changes, String delta) {
        assertTrue(changes.isEmpty(), "Expecting no ids from server at " + delta);
    }

    protected void assertResource(
        Map<String, DiffResource> changes,
        String name,
        String id,
        String version,
        DiffOpType op
    ) {
       assertResource(changes, name, id, version, op, "lastUpdate=null");
    }

    protected void assertResource(
        Map<String, DiffResource> changes,
        String name,
        String id,
        String version,
        DiffOpType op,
        String delta
    ) {
        assertTrue(changes.containsKey(id), format("Expected %s id not found after %s", name, delta));
        assertEquals(op, changes.get(id).op(), format("Expected %s as an %s op after %s", name, op.name(), delta));
        assertEquals(version, changes.get(id).version(), format("Expected %s version doesn't match", name));
    }

    protected void assertResourceSize(int size, Map<String, DiffResource> changes) {
        assertEquals(size, changes.size(), "Expected size doesn't match current one");
    }

}
