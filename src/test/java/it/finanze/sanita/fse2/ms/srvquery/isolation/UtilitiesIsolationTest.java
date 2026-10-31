/* SPDX-License-Identifier: AGPL-3.0-or-later */
package it.finanze.sanita.fse2.ms.srvquery.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.CustomCapabilityStatement;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.crypt.CryptUtility;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.interceptor.ClientTenantPartitionInterceptor;
import it.finanze.sanita.fse2.ms.srvquery.utility.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ISOLATION")
class UtilitiesIsolationTest {
    private final FhirContext context = FhirContext.forR4Cached();
    private final FHIRR4Helper helper = new FHIRR4Helper(context);
    private final FHIRUtility utility = new FHIRUtility(helper);

    @Test void masterIdentifiersAcceptBareAndCompositeValuesAndRejectMalformedInput() {
        assertEquals("document", StringUtility.getSearchParamFromMasterId("document"));
        assertEquals("document", StringUtility.getSearchParamFromMasterId("system^document"));
        assertTrue(StringUtility.isValidMasterId("document"));
        assertTrue(StringUtility.isValidMasterId("system^document"));
        for (String value : new String[]{null, "", " ", "^document", "system^", "system^document^extra", "system^ "}) {
            assertThrows(IllegalArgumentException.class, () -> StringUtility.getSearchParamFromMasterId(value));
        }
        assertFalse(StringUtility.isValidMasterId(" "));
        assertFalse(StringUtility.isValidMasterId("^document"));
        assertFalse(StringUtility.isValidMasterId("system^"));
        assertFalse(StringUtility.isValidMasterId("a^b^c"));
    }

    @Test void jsonRoundTripsAndInvalidJacksonInputReturnsNull() {
        var dto = new FhirPublicationDTO("id", "{}", "120");
        assertEquals("id", StringUtility.fromJSON(StringUtility.toJSON(dto), FhirPublicationDTO.class).getIdentifier());
        assertEquals("120", StringUtility.fromJSONJackson(StringUtility.toJSON(dto), FhirPublicationDTO.class).getRegion());
        assertNull(StringUtility.fromJSONJackson("invalid", FhirPublicationDTO.class));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", StringUtility.encodeSHA256("abc".getBytes(StandardCharsets.UTF_8)));
        assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256(null));
        var cause = new IllegalArgumentException("cause");
        assertSame(cause, new BusinessException(cause).getCause());
    }

    @Test void realFhirJsonXmlAndNullFormatRoundTripsAndClientConstruction() {
        Patient patient = new Patient();
        patient.setId("Patient/test");
        patient.addName().setFamily("Rossi");
        String json = helper.serializeResource(patient, true, false, false);
        Patient parsed = helper.deserializeResource(Patient.class, json, true);
        assertEquals("Rossi", parsed.getNameFirstRep().getFamily());
        String xml = context.newXmlParser().encodeResourceToString(patient);
        assertEquals("test", ((Patient) helper.deserializeResource(Patient.class, xml, false)).getIdElement().getIdPart());
        assertEquals("test", ((Patient) helper.deserializeResource(Patient.class, xml, null)).getIdElement().getIdPart());
        assertSame(context, helper.getContext());
        assertEquals("http://localhost:9999/fhir", helper.createClient("http://localhost:9999/fhir", "user", "password").getServerBase());
        FhirCFG configuration = new FhirCFG();
        configuration.setFhirServerUrl("http://localhost:9999/fhir");
        configuration.setFhirServerUser("user");
        configuration.setFhirServerPwd("password");
        assertEquals("user", configuration.getFhirServerUser());
        assertEquals("password", configuration.getFhirServerPwd());
        assertEquals(configuration.getFhirServerUrl(), configuration.fhirGenericClient(configuration.fhirContext()).getServerBase());
    }

    @Test void emptyUpdateClearsCategoryButRetainsEventsAndInvalidDatesFail() {
        DocumentReference reference = new DocumentReference();
        reference.addCategory().addCoding().setCode("old");
        reference.getContext().addEvent().addCoding().setCode("retained");
        utility.prepareForUpdate(reference, "{\"body\":{}}");
        assertTrue(reference.getCategory().isEmpty());
        assertEquals("retained", reference.getContext().getEventFirstRep().getCodingFirstRep().getCode());
        assertFalse(reference.getContext().getPeriod().hasStart());
        assertFalse(reference.getContext().getPeriod().hasEnd());
        assertThrows(BusinessException.class, () -> utility.prepareForUpdate(reference, "{\"body\":{\"dataInizioPrestazione\":\"bad\"}}"));
        assertThrows(BusinessException.class, () -> utility.prepareForUpdate(reference, "{}"));
    }

    @Test void deletionRetainsEveryImmutableResource() {
        Bundle bundle = new Bundle();
        for (Resource resource : List.of(new Patient(), new Practitioner(), new Organization(), new Location(), new Observation().setId("Observation/delete"))) {
            bundle.addEntry().setResource(resource).setFullUrl("urn:uuid:old").setSearch(new Bundle.BundleEntrySearchComponent());
        }
        utility.prepareForDelete(bundle, (DocumentReference) new DocumentReference().setId("DocumentReference/delete"));
        assertEquals(2, bundle.getEntry().size());
        assertEquals("Observation/delete", bundle.getEntryFirstRep().getRequest().getUrl());
        assertFalse(bundle.getEntryFirstRep().hasSearch());
        assertFalse(bundle.getEntryFirstRep().hasFullUrl());
    }

    @Test void patientEncryptionHandlesEmptyBundlesNonPatientsAndUnconditionalRequests() {
        CryptUtility.encryptPatientInfo(null);
        CryptUtility.encryptPatientInfo(new Bundle());
        Bundle bundle = new Bundle();
        bundle.addEntry().setResource(new Observation());
        Patient patient = new Patient();
        patient.addIdentifier().setValue("CF");
        var entry = bundle.addEntry().setResource(patient).setFullUrl("urn:uuid:patient");
        CryptUtility.encryptPatientInfo(bundle);
        assertFalse(patient.hasId());
        entry.getRequest().setUrl("Patient/id");
        CryptUtility.encryptPatientInfo(bundle);
        assertEquals("urn:uuid:patient", entry.getFullUrl());
        entry.getRequest().setUrl("Patient?identifier=CF");
        Observation observation = new Observation().setSubject(new Reference("urn:uuid:patient"));
        observation.addPerformer(new Reference("urn:uuid:patient"));
        observation.addPerformer(new Reference("Practitioner/other"));
        observation.addPerformer(new Reference());
        bundle.addEntry().setResource(observation);
        CryptUtility.encryptPatientInfo(bundle);
        assertTrue(patient.hasId());
        assertEquals(entry.getFullUrl(), observation.getSubject().getReference());
        assertEquals(entry.getFullUrl(), observation.getPerformer().get(0).getReference());
        assertEquals("Practitioner/other", observation.getPerformer().get(1).getReference());
        assertTrue(CryptUtility.getAllReferences(observation).contains(observation.getSubject()));
    }

    @Test void tenantHeaderIsThreadLocalAndCleared() throws Exception {
        ClientTenantPartitionInterceptor interceptor = new ClientTenantPartitionInterceptor();
        IHttpRequest request = mock(IHttpRequest.class);
        interceptor.interceptRequest(request);
        interceptor.setTenantId("");
        interceptor.interceptRequest(request);
        verifyNoInteractions(request);
        interceptor.setTenantId("120");
        Thread other = new Thread(() -> interceptor.interceptRequest(request));
        other.start(); other.join();
        verifyNoInteractions(request);
        interceptor.interceptRequest(request);
        verify(request).addHeader("X-Tenant-ID", "120");
        interceptor.clearTenantId();
        interceptor.interceptRequest(request);
        interceptor.interceptResponse(null);
        verifyNoMoreInteractions(request);
    }

    @Test void customCapabilitiesExposeTypedSearchPaths() {
        CustomCapabilityStatement capabilities = new CustomCapabilityStatement();
        var paths = List.of(new StringType("Observation.code"));
        var resource = new CustomCapabilityStatement.CustomCapabilityStatementRestResourceComponent("Observation", paths);
        capabilities.setResourceSearchPaths(List.of(resource));
        assertEquals("Observation", capabilities.getResourceSearchPaths().getFirst().getType());
        assertSame(paths, resource.getSearchPath());
        assertTrue(new CustomCapabilityStatement.CustomCapabilityStatementRestResourceComponent("Patient").getSearchPath().isEmpty());
        assertNull(new CustomCapabilityStatement.CustomCapabilityStatementRestResourceComponent().getSearchPath());
    }
}
