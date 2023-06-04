package it.finanze.sanita.fse2.ms.srvquery.diff.others;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class CSDiffCalculator {

        public static String OP_ADD = "ADD";
        public static String OP_REMOVE = "REMOVE";
        public static String OP_UNKNOWN = "?";

        public static void main(String[] args) {

            String server = "http://localhost:8080/fhir/";

            TerminologyClient fhir = new TerminologyClient(server, "admin", "admin");

            Date lastUpdate = Date.from(LocalDateTime.of(
                2023, 5, 30, 12, 3, 0
            ).toInstant(ZoneOffset.ofHours(2)));

            Pair<Date, Map<String, Map<String, List<String>>>> tNull = createChangeset(fhir, null);
            Pair<Date, Map<String, Map<String, List<String>>>> t0 = createChangeset(fhir, lastUpdate);

            printChangeset(tNull);
            printChangeset(t0);
        }

    @NotNull
    public static Pair<Date, Map<String, Map<String, List<String>>>> createChangeset(TerminologyClient fhir, Date lastUpdate) {
        // Pair containing lastUpdate (Date) and a mapping with each changed code system and the operation to perform
        Pair<Date, Map<String, Map<String, List<String>>>> alignment = Pair.of(lastUpdate, new HashMap<>());
        // Find code systems modified since that time (if null, should get everything)
        List<String> systems = fhir.searchModifiedCodeSystem(lastUpdate).stream().map(
            cs -> cs.getIdElement().getIdPart()
        ).collect(Collectors.toList());
        // Obtain map
        Map<String, Map<String, List<String>>> map = alignment.getValue();
        // For each modified code-system, check what changed
        for (String system : systems) {
            // Obtain Map<OperationToPerform, CodesAffected> (if null, nothing changed)
            Map<String, List<String>> codes = new CSDiffCalculator().calculateCodeSystemUpdates(
            	fhir,
                system,
                lastUpdate
            );
            if(codes != null) {
                // Insert code system into map
                map.put(system, new HashMap<>());
                // Iterate on each operation to perform
                for (Map.Entry<String, List<String>> op : codes.entrySet()) {
                    // Iterate on each value
                    map.get(system).put(op.getKey(), op.getValue());
                }
            }
        }
        return alignment;
    }

    private static void printChangeset(Pair<Date, Map<String, Map<String, List<String>>>> alignment) {
        Date lastUpdate = alignment.getKey();
        String date = lastUpdate != null ? lastUpdate.toInstant().atZone(ZoneOffset.ofHours(2)).toString() : "null";
        System.out.println("lastUpdate is " + date);
        System.out.println("The following code-systems were updated: " + alignment.getValue().size());
        System.out.println();
        Map<String, Map<String, List<String>>> cs = alignment.getValue();
        for (Map.Entry<String, Map<String, List<String>>> e : cs.entrySet()) {
            String cs0 = e.getKey();
            Map<String, List<String>> changeset = e.getValue();
            System.out.println("CS Id: " + cs0);
            System.out.println("Changeset");
            for (Map.Entry<String, List<String>> entry : changeset.entrySet()) {
                String op = entry.getKey();
                List<String> codes = entry.getValue();
                String symbol = OP_UNKNOWN;
                if (op.equals(OP_ADD)) {
                    symbol = "+";
                }
                if (op.equals(OP_REMOVE)) {
                    symbol = "-";
                }
                for (String code : codes) {
                    System.out.printf("%s %s", symbol, code);
                    System.out.println();
                }
            }
            System.out.println();
        }
    }

    private List<String> getCodeList(CodeSystem codeSystem) {
        List<String> codes = new ArrayList<>();
        if (codeSystem!=null && codeSystem.hasConcept()) {
            for (CodeSystem.ConceptDefinitionComponent concept : codeSystem.getConcept()) {
                codes.add(concept.getCode());
            }
        }
        return codes;
    }

    public Map<String, List<String>> calculateCodeSystemUpdates(TerminologyClient client, String id, Date date) {
        Map<String, List<String>> codes;
        if(date != null) {
            codes = calculateCodeSystemUpdatesWithDate(client, id, date);
        } else {
            codes = calculateCodeSystemUpdatesNoDate(client, id);
        }
        return codes;
    }

    public Map<String, List<String>> calculateCodeSystemUpdatesWithDate(TerminologyClient client, String id, Date date) {
        Map<String, List<String>> out;
        // Old version of CodeSystem
        CodeSystem oldCS = client.getCodeSystemVersionByIdAndDate(id, date);
        List<String> codesOldCS = getCodeList(oldCS);

        // Last version of CodeSystem
        CodeSystem lastCS = client.readCS(id);
        List<String> codesLastCS = getCodeList(lastCS);

        // Codes to ADD: present in the last version but not in previous version
        List<String> codesToInsert = new ArrayList<>(codesLastCS);
        codesToInsert.removeAll(codesOldCS);

        // Codes to DELETE: present in the previous version but not in last version
        List<String> codesToDelete = new ArrayList<>(codesOldCS);
        codesToDelete.removeAll(codesLastCS);

        if(codesToInsert.isEmpty() && codesToDelete.isEmpty()) {
            out = null;
        } else {
            out = new HashMap<>();
            out.put(OP_ADD, codesToInsert);
            out.put(OP_REMOVE, codesToDelete);
        }

        return out;
    }

    public Map<String, List<String>> calculateCodeSystemUpdatesNoDate(TerminologyClient client, String id) {
        // Last version of CodeSystem
        CodeSystem lastCS = client.readCS(id);
        List<String> codesLastCS = getCodeList(lastCS);

        Map<String, List<String>> out = new HashMap<>();
        out.put(OP_ADD, new ArrayList<>(codesLastCS));
        out.put(OP_REMOVE, new ArrayList<>());

        return out;
    }

}
