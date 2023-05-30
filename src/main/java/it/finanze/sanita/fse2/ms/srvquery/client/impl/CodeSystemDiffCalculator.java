package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class CodeSystemDiffCalculator {

        public static String OP_ADD = "ADD";
        public static String OP_REMOVE = "REMOVE";
        public static String OP_UNKNOWN = "?";

        public static void main(String[] args) {

            String server = "http://localhost:8080/fhir/";

            FHIRClient fhir = new FHIRClient(server, "admin", "admin");
            IGenericClient client = FHIRR4Helper.createClient(server, "admin", "admin");

            Date lastUpdate = Date.from(LocalDateTime.of(
                2023, 5, 30, 12, 3, 0
            ).toInstant(ZoneOffset.ofHours(2)));

            Pair<Date, Map<CodeSystem, Map<String, List<String>>>> tNull = createChangeset(fhir, client, null);
            Pair<Date, Map<CodeSystem, Map<String, List<String>>>> t0 = createChangeset(fhir, client, lastUpdate);

            printChangeset(tNull);
            printChangeset(t0);
        }

    @NotNull
    private static Pair<Date, Map<CodeSystem, Map<String, List<String>>>> createChangeset(FHIRClient fhir, IGenericClient client, Date lastUpdate) {
        // Pair containing lastUpdate (Date) and a mapping with each changed code system and the operation to perform
        Pair<Date, Map<CodeSystem, Map<String, List<String>>>> alignment = Pair.of(lastUpdate, new HashMap<>());
        // Find code systems modified since that time (if null, should get everything)
        List<CodeSystem> systems = fhir.searchModifiedCodeSystem(lastUpdate);
        // Obtain map
        Map<CodeSystem, Map<String, List<String>>> map = alignment.getValue();
        // For each modified code-system, check what changed
        for (CodeSystem system : systems) {
            // Obtain Map<OperationToPerform, CodesAffected> (if null, nothing changed)
            Map<String, List<String>> codes = new CodeSystemDiffCalculator().calculateCodeSystemUpdates(
                client,
                system.getId(),
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

    private static void printChangeset(Pair<Date, Map<CodeSystem, Map<String, List<String>>>> alignment) {
        Date lastUpdate = alignment.getKey();
        String date = lastUpdate != null ? lastUpdate.toInstant().atZone(ZoneOffset.ofHours(2)).toString() : "null";
        System.out.println("lastUpdate is " + date);
        System.out.println("The following code-systems were updated: " + alignment.getValue().size());
        System.out.println();
        Map<CodeSystem, Map<String, List<String>>> cs = alignment.getValue();
        for (Map.Entry<CodeSystem, Map<String, List<String>>> e : cs.entrySet()) {
            CodeSystem cs0 = e.getKey();
            Map<String, List<String>> changeset = e.getValue();
            System.out.println("CS Name: " + cs0.getName());
            System.out.println("CS Identifier: " + cs0.getIdBase());
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

    private CodeSystem getCodeSystemLastVersion(IGenericClient client, String id) {
            return client.read().resource(CodeSystem.class).withId(id).execute();
    }

    private CodeSystem getCodeSystemVersionByIdAndDate(IGenericClient client, String id, Date date) {
        CodeSystem out = null;
        Bundle resultBundle = client
            .search()
            .forResource(CodeSystem.class)
            .where(CodeSystem.RES_ID.exactly().code(id))
            .and(new DateClientParam("_lastUpdated").beforeOrEquals().millis(date))
            .sort().descending("_lastUpdated")
            .returnBundle(Bundle.class)
            .execute();

        List<Bundle.BundleEntryComponent> entries = resultBundle.getEntry();
        if (!entries.isEmpty()) {
            out = (CodeSystem) entries.get(0).getResource();
        }
        return out;
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

    public Map<String, List<String>> calculateCodeSystemUpdates(IGenericClient client, String id, Date date) {
        Map<String, List<String>> codes;
        if(date != null) {
            codes = calculateCodeSystemUpdatesWithDate(client, id, date);
        } else {
            codes = calculateCodeSystemUpdatesNoDate(client, id);
        }
        return codes;
    }

    public Map<String, List<String>> calculateCodeSystemUpdatesWithDate(IGenericClient client, String id, Date date) {
        Map<String, List<String>> out;
        // Old version of CodeSystem
        CodeSystem oldCS = getCodeSystemVersionByIdAndDate(client, id, date);
        List<String> codesOldCS = getCodeList(oldCS);

        // Last version of CodeSystem
        CodeSystem lastCS = getCodeSystemLastVersion(client, id);
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

    public Map<String, List<String>> calculateCodeSystemUpdatesNoDate(IGenericClient client, String id) {
        // Last version of CodeSystem
        CodeSystem lastCS = getCodeSystemLastVersion(client, id);
        List<String> codesLastCS = getCodeList(lastCS);

        Map<String, List<String>> out = new HashMap<>();
        out.put(OP_ADD, new ArrayList<>(codesLastCS));
        out.put(OP_REMOVE, new ArrayList<>());

        return out;
    }

}
