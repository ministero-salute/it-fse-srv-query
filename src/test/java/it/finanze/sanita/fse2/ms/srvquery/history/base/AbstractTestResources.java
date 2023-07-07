package it.finanze.sanita.fse2.ms.srvquery.history.base;

import static java.lang.String.format;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.DRAFT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.params.provider.Arguments;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.impl.CSBuilder;
import it.finanze.sanita.fse2.ms.srvquery.history.crud.dto.impl.VSBuilder;

public abstract class AbstractTestResources {
    protected BaseResource createGenderTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.5.1");
        builder.addCodes("M", "Male");
        builder.addCodes("F", "Female");
        return builder.build();
    }

    protected BaseResource createGenderTestCS(PublicationStatus id) {
        CodeSystem cs = (CodeSystem) createGenderTestCS();
        cs.setStatus(id);
        return cs;
    }
    
    protected BaseResource createOreTestCS(PublicationStatus id) {
        CodeSystem cs = (CodeSystem) createOreTestCS();
        cs.setStatus(id);
        return cs;
    }

    protected BaseResource createOreTestCS() {
        CSBuilder builder = new CSBuilder("2.16.840.1.113883.2.9.6.1.54.6");
        builder.addCodes("P", "Platinum");
        builder.addCodes("D", "Diamond");
        builder.addCodes("G", "Gold");
        return builder.build();
    }

    protected BaseResource createColorsTestVS() {
        VSBuilder builder = new VSBuilder(
            "2.144.33.22.04.38",
            "http://url/colors"
        );
        builder.addInclusionCS("http://url/primary-colors");
        builder.addInclusionCS("http://url/secondary-colors");
        return builder.build();
    }

    protected BaseResource createColorsTestVS(PublicationStatus id) {
        ValueSet cs = (ValueSet) createColorsTestVS();
        cs.setStatus(id);
        return cs;
    }
    
    protected BaseResource createDaysTestVS(PublicationStatus id) {
        ValueSet cs = (ValueSet) createDaysTestVS();
        cs.setStatus(id);
        return cs;
    }

    protected BaseResource createDaysTestVS() {
        VSBuilder builder = new VSBuilder(
            "2.144.33.12.02.38",
            "http://url/days"
        );
        builder.addCodes("M", "Monday");
        builder.addCodes("T", "Tuesday");
        builder.addCodes("W", "Wednesday");
        return builder.build();
    }

    protected BaseResource createPrimaryColorsTestCS() {
        CSBuilder builder = new CSBuilder("2.144.33.22.04.39");
        builder.addUrl("http://url/primary-colors");
        builder.addCodes("R", "Red");
        builder.addCodes("Y", "Yellow");
        builder.addCodes("B", "Blue");
        return builder.build();
    }

    protected BaseResource createSecondaryColorsTestCS() {
        CSBuilder builder = new CSBuilder("2.144.33.22.04.40");
        builder.addUrl("http://url/secondary-colors");
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

    protected Stream<Arguments> getTestResources() {
        return Stream.of(
            Arguments.of((Object) getTestResourcesCS()),
            Arguments.of((Object) getTestResourcesVS()),
            Arguments.of((Object) getTestResourcesMixed())
        );
    }

    protected Stream<Arguments> getTestResourcesDraft() {
    	return Stream.of(
            Arguments.of((Object) getTestResourcesCSDraft()),
            Arguments.of((Object) getTestResourcesVSDraft()),
            Arguments.of((Object) getTestResourcesMixedDraft())
        );
    }

    protected TestResource[] getTestResourcesMixed() {
        return new TestResource[] {
            new TestResource("gender", createGenderTestCS()),
            new TestResource("days", createDaysTestVS())
        };
    }

    private TestResource[] getTestResourcesCS() {
        return new TestResource[] {
            new TestResource("gender", createGenderTestCS()),
            new TestResource("ore", createOreTestCS())
        };
    }

    private TestResource[] getTestResourcesVS() {
        return new TestResource[] {
            new TestResource("colors", createColorsTestVS()),
            new TestResource("days", createDaysTestVS())
        };
    }
    
    protected TestResource[] getTestResourcesMixedDraft() {
        return new TestResource[] {
            new TestResource("gender", createGenderTestCS(DRAFT)),
            new TestResource("days", createDaysTestVS(DRAFT))
        };
    }

    private TestResource[] getTestResourcesCSDraft() {
        return new TestResource[] {
            new TestResource("gender", createGenderTestCS(DRAFT)),
            new TestResource("ore", createOreTestCS(DRAFT))
        };
    }

    private TestResource[] getTestResourcesVSDraft() {
        return new TestResource[] {
            new TestResource("colors", createColorsTestVS(DRAFT)),
            new TestResource("days", createDaysTestVS(DRAFT))
        };
    }

}
