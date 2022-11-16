package it.finanze.sanita.fse2.ms.srvquery.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentRelationshipType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;

public class FHIRUtility {

	public static final List<Class<?>> IMMUTABLE_RESOURCES = Arrays.asList(
			Patient.class,
			Practitioner.class,
			PractitionerRole.class,
			Organization.class,
			Location.class
			);
	
	public static Bundle deserializeBundle(String json) {
		return FHIRR4Helper.deserializeResource(Bundle.class, json, true);		
	}

	public static void prepareForDelete(Bundle bundle, DocumentReference documentReference) {
		bundle.setType(BundleType.TRANSACTION);
		bundle.getEntry().add(getBundleEntryComponent(documentReference));
		bundle.getEntry().removeIf(FHIRUtility::isImmutable);
		bundle.getEntry().forEach(FHIRUtility::setDeletionRequest);
	}

	public static void prepareForReplace(Bundle bundle, DocumentReference previousDocumentReference, Bundle previousDocument) {
    	setRelatedDocumentReference(bundle, previousDocumentReference);
    	addResourcesToDelete(bundle, previousDocumentReference, previousDocument);
	}
	
	public static void prepareForUpdate(DocumentReference documentReference, String jsonString) {
    	// TODO 
    	documentReference.getCategory().get(0).getCoding().get(0).setCode("livBasso");
	}

	private static void setDeletionRequest(BundleEntryComponent entry) {
		BundleEntryRequestComponent request = new BundleEntryRequestComponent();
		request.setMethod(HTTPVerb.DELETE);
		request.setUrl(getUrl(entry));
		entry.setRequest(request);
	}

	private static String getUrl(BundleEntryComponent entry) {
		IdType idType = entry.getResource().getIdElement();;
		return idType.getResourceType() + "/" + idType.getIdPart();
	}

    private static void setRelatedDocumentReference(Bundle bundle, DocumentReference documentReference) {
    	String documentReferenceId = documentReference.getId();
    	bundle
		.getEntry()
		.stream()
		.filter(entry -> (entry.getResource() instanceof DocumentReference))
		.forEach(entry -> setRelatedDocumentReference(entry.getResource(), documentReferenceId));
	}

	private static void setRelatedDocumentReference(Resource documentReference, String previousIdentifier) {
		DocumentReference reference = (DocumentReference) documentReference;
		if (reference.getRelatesTo() == null) reference.setRelatesTo(new ArrayList<>());
		DocumentReferenceRelatesToComponent related = getRelatedDocumentReference(previousIdentifier);
		reference.getRelatesTo().add(related);
	}

	private static DocumentReferenceRelatesToComponent getRelatedDocumentReference(String previousIdentifier) {
		DocumentReferenceRelatesToComponent related = new DocumentReferenceRelatesToComponent();
		DocumentRelationshipType code = DocumentRelationshipType.REPLACES;
		related.setCode(code);
		related.setId(previousIdentifier);
		return related;
	}
	
	private static void addResourcesToDelete(Bundle bundle, DocumentReference previousDocumentReference, Bundle previousDocument) {
		prepareForDelete(previousDocument, previousDocumentReference);
		bundle.getEntry().addAll(previousDocument.getEntry());
	}
	
	private static BundleEntryComponent getBundleEntryComponent(DocumentReference documentReference) {
		BundleEntryComponent component = new BundleEntryComponent();
		component.setResource(documentReference);
		return component;
	}

	private static boolean isImmutable(BundleEntryComponent bundleEntryComponent) {
		return isImmutable(bundleEntryComponent.getResource());
	}

	private static boolean isImmutable(Resource resource) {
		return IMMUTABLE_RESOURCES
				.stream()
				.anyMatch(immutableResource -> resource.getClass() == immutableResource);
	}
	
}
