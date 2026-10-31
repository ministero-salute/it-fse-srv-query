/* SPDX-License-Identifier: AGPL-3.0-or-later */
package it.finanze.sanita.fse2.ms.srvquery.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import brave.Tracing;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.*;
import it.finanze.sanita.fse2.ms.srvquery.config.*;
import it.finanze.sanita.fse2.ms.srvquery.controller.impl.*;
import it.finanze.sanita.fse2.ms.srvquery.controller.handler.ExceptionCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.impl.FHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.*;

/** Real HTTP/controller/service/FHIR transformations; only the remote HAPI boundary is mocked. */
@Tag("ISOLATION")
@org.springframework.test.context.ActiveProfiles("ISOLATION")
@SpringJUnitConfig(ApplicationIsolationTest.Context.class)
@TestPropertySource(properties = {"fhir-server-url=http://localhost:9999/fhir", "fhir-server-user=user", "fhir-server-pwd=secret"})
class ApplicationIsolationTest {
    @org.springframework.context.annotation.Profile("ISOLATION")
    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    @Import({FhirCFG.class, FHIRR4Helper.class, FHIRUtility.class, FHIRClient.class, FHIRSRV.class,
            DocumentCTL.class, SearchParamsCTL.class, LivenessCheckCTL.class, ExceptionCTL.class})
    static class Context {
        @Bean(destroyMethod = "close") Tracing tracing() { return Tracing.newBuilder().build(); }
        @Bean brave.Tracer tracer(Tracing tracing) { return tracing.tracer(); }
    }

    @MockitoBean(name = "fhirGenericClient", answers = Answers.RETURNS_DEEP_STUBS)
    IGenericClient remote;
    @Autowired FHIRClient client;
    @Autowired FHIRSRV service;
    @Autowired FHIRR4Helper helper;
    @Autowired DocumentCTL documents;
    @Autowired SearchParamsCTL searches;
    @Autowired LivenessCheckCTL liveness;
    @Autowired ExceptionCTL errors;
    @Autowired brave.Tracer tracer;
    MockMvc mvc;
    AtomicReference<Bundle> sent;

    @BeforeEach void setup() {
        reset(remote);
        queries.clear();
        mvc = MockMvcBuilders.standaloneSetup(documents, searches, liveness).setControllerAdvice(errors).build();
        sent = new AtomicReference<>();
        var identifierQuery = mock(ca.uhn.fhir.rest.gclient.IQuery.class, RETURNS_SELF);
        when(remote.search().forResource(DocumentReference.class)).thenReturn(identifierQuery);
        when(remote.search().byUrl(anyString())).thenAnswer(invocation ->
                queries.computeIfAbsent(invocation.getArgument(0), key -> mock(ca.uhn.fhir.rest.gclient.IQuery.class, RETURNS_SELF)));
        var update = mock(ca.uhn.fhir.rest.gclient.IUpdateTyped.class, RETURNS_SELF);
        when(remote.update().resource(any(DocumentReference.class))).thenReturn(update);
        transactionStep = mock(ca.uhn.fhir.rest.gclient.ITransactionTyped.class, RETURNS_SELF);
        // Capture the real, transformed transaction at the external boundary.
        when(remote.transaction().withBundle(any(Bundle.class))).thenAnswer(invocation -> {
            sent.set(invocation.getArgument(0));
            return transactionStep;
        });
    }

    private ca.uhn.fhir.rest.gclient.ITransactionTyped<Bundle> transactionStep;
    private final java.util.Map<String, ca.uhn.fhir.rest.gclient.IQuery> queries = new java.util.HashMap<>();

    private void transactionResult(Bundle result) {
        // Configure a stable fluent request, keeping business code and all FHIR resources real.
        when(transactionStep.execute()).thenReturn(result);
    }

    private Bundle searchResult(Bundle result) {
        when(remote.search().forResource(DocumentReference.class).cacheControl(any())
                .where(any(ca.uhn.fhir.rest.gclient.ICriterion.class)).withAdditionalHeader("X-Tenant-ID", "120").returnBundle(Bundle.class).execute()).thenReturn(result);
        return result;
    }

    private DocumentReference existing() {
        DocumentReference reference = new DocumentReference();
        reference.setId("DocumentReference/old");
        reference.getContext().addRelated(new Reference("Composition/old"));
        searchResult(new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(new Patient()))
                .addEntry(new Bundle.BundleEntryComponent().setResource(reference)));
        Bundle old = new Bundle();
        old.addEntry().setResource(new Composition().setId("Composition/old"));
        old.addEntry().setResource(new Observation().setId("Observation/old"));
        old.addEntry().setResource(new Patient().setId("Patient/retained"));
        when(remote.search().byUrl("http://localhost:9999/fhir/Composition/old/$document")
                .withAdditionalHeader("X-Tenant-ID", "120").execute()).thenReturn(old);
        return reference;
    }

    private FhirPublicationDTO publication() {
        Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        Patient patient = new Patient();
        patient.addIdentifier().setValue("RSSMRA80A01H501U");
        bundle.addEntry().setFullUrl("urn:uuid:patient").setResource(patient).getRequest().setUrl("Patient?identifier=RSSMRA80A01H501U");
        Composition composition = new Composition();
        composition.setId("Composition/new");
        composition.setSubject(new Reference("urn:uuid:patient"));
        bundle.addEntry().setResource(composition);
        bundle.addEntry().setResource(new DocumentReference().setId("DocumentReference/new"));
        return new FhirPublicationDTO("system^document", helper.serializeResource(bundle, false, true, false), "120");
    }

    private String json(FhirPublicationDTO body) { return StringUtility.toJSON(body); }

    @Test void createsDocumentAndHashesPatientAndReferences() throws Exception {
        transactionResult((Bundle) new Bundle().setId("Bundle/created"));
        mvc.perform(post("/v1/document/create").contentType(MediaType.APPLICATION_JSON).content(json(publication())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        Bundle bundle = sent.get();
        Patient patient = (Patient) bundle.getEntryFirstRep().getResource();
        assertEquals(StringUtility.encodeSHA256("RSSMRA80A01H501U".getBytes(java.nio.charset.StandardCharsets.UTF_8)), patient.getIdElement().getIdPart());
        assertEquals("Patient/" + patient.getIdElement().getIdPart(), bundle.getEntryFirstRep().getRequest().getUrl());
        assertEquals(bundle.getEntryFirstRep().getFullUrl(), ((Composition) bundle.getEntry().get(1).getResource()).getSubject().getReference());
        verify(transactionStep).withAdditionalHeader("X-Tenant-ID", "120");
    }

    @Test void deletesOnlyMutableResourcesAndDocumentReference() throws Exception {
        transactionResult((Bundle) new Bundle().setId("Bundle/deleted"));
        existing();
        mvc.perform(delete("/v1/document/delete/system^document").param("region", "120"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        Bundle actual = sent.get();
        assertEquals(Bundle.BundleType.TRANSACTION, actual.getType());
        assertEquals(3, actual.getEntry().size());
        assertEquals(List.of("Composition/old", "Observation/old", "DocumentReference/old"),
                actual.getEntry().stream().map(e -> e.getRequest().getUrl()).toList());
        actual.getEntry().forEach(e -> { assertEquals(Bundle.HTTPVerb.DELETE, e.getRequest().getMethod()); assertFalse(e.hasFullUrl()); assertFalse(e.hasSearch()); });
    }

    @Test void replacesDocumentAndLinksPreviousCompositionAndReference() throws Exception {
        transactionResult((Bundle) new Bundle().setId("Bundle/replaced"));
        existing();
        mvc.perform(put("/v1/document/replace").contentType(MediaType.APPLICATION_JSON).content(json(publication())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        Bundle actual = sent.get();
        assertEquals(6, actual.getEntry().size());
        Composition composition = (Composition) actual.getEntry().get(1).getResource();
        assertEquals("Composition/old", composition.getRelatesToFirstRep().getId());
        assertEquals(Composition.DocumentRelationshipType.REPLACES, composition.getRelatesToFirstRep().getCode());
        DocumentReference reference = (DocumentReference) actual.getEntry().get(2).getResource();
        assertEquals("DocumentReference/old", reference.getRelatesToFirstRep().getId());
        assertEquals(DocumentReference.DocumentRelationshipType.REPLACES, reference.getRelatesToFirstRep().getCode());
    }

    @Test void updatesMetadataInPlace() throws Exception {
        DocumentReference reference = existing();
        when(remote.update().resource(reference).withAdditionalHeader("X-Tenant-ID", "120").execute())
                .thenReturn(new MethodOutcome(new IdType("DocumentReference/old")));
        FhirPublicationDTO body = new FhirPublicationDTO("system^document", """
                {"body":{"tipoDocumentoLivAlto":"REPORT","tipologiaStruttura":"HOSPITAL",
                "attiCliniciRegoleAccesso":["EVENT"],"assettoOrganizzativo":"PRACTICE",
                "dataInizioPrestazione":"20240101120000","dataFinePrestazione":"20240101130000"}}
                """, "120");
        mvc.perform(put("/v1/document/metadata/document").contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(true));
        assertEquals("REPORT", reference.getCategoryFirstRep().getCodingFirstRep().getCode());
        assertEquals("HOSPITAL", reference.getContext().getFacilityType().getCodingFirstRep().getCode());
        assertEquals("EVENT", reference.getContext().getEventFirstRep().getCodingFirstRep().getCode());
        assertEquals("PRACTICE", reference.getContext().getPracticeSetting().getCodingFirstRep().getCode());
        assertTrue(reference.getContext().getPeriod().getEnd().after(reference.getContext().getPeriod().getStart()));
    }

    @Test void existenceHandlesPresentAbsentAndEmptyResultsWithTrace() throws Exception {
        searchResult(null);
        assertFalse(service.checkExists("system^document", "120"));
        searchResult(new Bundle());
        mvc.perform(get("/v1/document/check-exist/document/120")).andExpect(status().isOk()).andExpect(jsonPath("$.exist").value(false));
        existing();
        var span = tracer.nextSpan().start();
        try (var scope = tracer.withSpanInScope(span)) {
            mvc.perform(get("/v1/document/check-exist/document/120")).andExpect(status().isOk())
                    .andExpect(jsonPath("$.exist").value(true)).andExpect(jsonPath("$.traceID").value(span.context().traceIdString()))
                    .andExpect(jsonPath("$.spanID").value(span.context().spanIdString()));
        } finally { span.finish(); }
        assertThrows(BusinessException.class, () -> service.checkExists(null, "120"));
        assertThrows(BusinessException.class, () -> service.checkExists("", "120"));
    }

    @Test void searchParametersAreGroupedDeduplicatedSortedAndIgnoreOtherResources() throws Exception {
        Bundle bundle = new Bundle().setTotal(4);
        bundle.addEntry().setResource(new Patient());
        for (String path : List.of("Observation.value", "Observation.code", "Observation.value")) {
            SearchParameter parameter = new SearchParameter().setExpression(path);
            parameter.addBase("Observation");
            parameter.addBase("Resource");
            bundle.addEntry().setResource(parameter);
        }
        when(remote.search().byUrl("http://localhost:9999/fhir/SearchParameter").withAdditionalHeader("X-Tenant-ID", "DEFAULT").execute()).thenReturn(bundle);
        mvc.perform(get("/v1/searchParams")).andExpect(status().isOk()).andExpect(jsonPath("$.params.length()").value(2));
        var result = service.getResourcesSearchParameters();
        assertEquals(java.util.Set.of("Observation", "Resource"), result.stream().map(p -> p.getName()).collect(java.util.stream.Collectors.toSet()));
        result.forEach(p -> assertEquals(List.of("Observation.code", "Observation.value"), p.getParameters()));
        mvc.perform(get("/status")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test void missingDocumentsAndUnsuccessfulTransactionsReturnFalse() {
        transactionResult(null);
        assertFalse(service.create(publication()));
        transactionResult(new Bundle());
        assertFalse(service.create(publication()));
        transactionResult((Bundle) new Bundle().setId("Bundle/success"));
        searchResult(null);
        assertFalse(service.delete("document", "120"));
        searchResult(new Bundle());
        assertNull(client.getDocumentReferenceBundle("document", "120"));
        searchResult(new Bundle().addEntry(new Bundle.BundleEntryComponent().setResource(new Patient())));
        assertNull(client.getDocumentReferenceBundle("document", "120"));
    }

    @Test void allMutationFailuresAreReturnedAsUnsuccessfulResponses() throws Exception {
        transactionResult(new Bundle());
        when(transactionStep.execute()).thenThrow(new IllegalStateException("remote unavailable"));
        mvc.perform(post("/v1/document/create").contentType(MediaType.APPLICATION_JSON).content(json(publication())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false)).andExpect(jsonPath("$.message").value("remote unavailable"));
        existing();
        mvc.perform(delete("/v1/document/delete/document").param("region", "120"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false)).andExpect(jsonPath("$.message").isString());
        mvc.perform(put("/v1/document/replace").contentType(MediaType.APPLICATION_JSON).content(json(publication())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false));
        FhirPublicationDTO malformed = new FhirPublicationDTO("document", "not-json", "120");
        mvc.perform(post("/v1/document/create").contentType(MediaType.APPLICATION_JSON).content(json(malformed)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false));
        mvc.perform(put("/v1/document/metadata/document").contentType(MediaType.APPLICATION_JSON).content(json(malformed)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false));
    }

    @Test void uncaughtFailuresBecomeProblemJsonWithAndWithoutTrace() throws Exception {
        when(remote.search().forResource(DocumentReference.class).cacheControl(any()).where(any(ca.uhn.fhir.rest.gclient.ICriterion.class))
                .withAdditionalHeader("X-Tenant-ID", "120").returnBundle(Bundle.class).execute()).thenThrow(new IllegalStateException("lookup failed"));
        mvc.perform(get("/v1/document/check-exist/document/120")).andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON)).andExpect(jsonPath("$.detail").value("lookup failed"))
                .andExpect(jsonPath("$.instance").value("/internal"));
        var span = tracer.nextSpan().start();
        try (var scope = tracer.withSpanInScope(span)) {
            mvc.perform(get("/v1/document/check-exist/document/120")).andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.traceID").value(span.context().traceIdString())).andExpect(jsonPath("$.spanID").value(span.context().spanIdString()));
        } finally { span.finish(); }
        assertThrows(BusinessException.class, () -> client.getDocumentReferenceBundle("document", "120"));
        assertThrows(BusinessException.class, () -> service.delete("document", "120"));
        assertThrows(BusinessException.class, () -> service.replace(publication()));
        assertThrows(BusinessException.class, () -> service.updateMetadata(publication()));
    }

    @Test void clientWrapsRemoteReadUpdateAndCapabilityFailures() {
        when(remote.search().byUrl("url/Composition/id/$document").withAdditionalHeader("X-Tenant-ID", "120").execute()).thenThrow(new IllegalStateException("read failed"));
        assertThrows(BusinessException.class, () -> client.getDocument("Composition/id", "url", "120"));
        when(remote.search().byUrl("http://localhost:9999/fhir/SearchParameter").withAdditionalHeader("X-Tenant-ID", "DEFAULT").execute()).thenThrow(new IllegalStateException("params failed"));
        assertThrows(BusinessException.class, client::getSearchParams);
        var capabilities = new CustomCapabilityStatement();
        when(remote.capabilities().ofType(CustomCapabilityStatement.class).execute()).thenReturn(capabilities);
        assertSame(capabilities, client.getServerCapabilities());
        when(remote.capabilities().ofType(CustomCapabilityStatement.class).execute()).thenThrow(new IllegalStateException("capabilities failed"));
        assertThrows(BusinessException.class, client::getServerCapabilities);
        var reference = new DocumentReference();
        when(remote.update().resource(reference).withAdditionalHeader("X-Tenant-ID", "120").execute()).thenThrow(new IllegalStateException("update failed"));
        assertThrows(BusinessException.class, () -> client.update(reference, "120"));
        var updateRequest = remote.update().resource(reference).withAdditionalHeader("X-Tenant-ID", "120");
        doReturn(new MethodOutcome(new IdType(""))).when(updateRequest).execute();
        assertFalse(client.update(reference, "120"));
    }

    @Test void updateRemoteFailureAndMalformedReplacementAreHandledAtHttpBoundary() throws Exception {
        DocumentReference reference = existing();
        when(remote.update().resource(reference).withAdditionalHeader("X-Tenant-ID", "120").execute())
                .thenThrow(new IllegalStateException("update unavailable"));
        var metadata = new FhirPublicationDTO("document", "{\"body\":{}}", "120");
        mvc.perform(put("/v1/document/metadata/document").contentType(MediaType.APPLICATION_JSON).content(json(metadata)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false)).andExpect(jsonPath("$.message").value("Error while perform update operation : "));
        mvc.perform(put("/v1/document/replace").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new FhirPublicationDTO("document", "invalid", "120"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.esito").value(false));
    }
}
