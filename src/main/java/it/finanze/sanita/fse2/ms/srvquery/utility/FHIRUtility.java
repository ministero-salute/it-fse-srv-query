/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.srvquery.utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionRelatesToComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentRelationshipType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;

import com.google.gson.internal.LinkedTreeMap;

import it.finanze.sanita.fse2.ms.srvquery.dto.UpdateBodyDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FHIRUtility {

	public static final List<Class<?>> IMMUTABLE_RESOURCES = Arrays.asList(
			Patient.class,
			Practitioner.class,
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
    	setRelatedComposition(bundle, previousDocument);
    	addResourcesToDelete(bundle, previousDocumentReference, previousDocument);
	}
	
	@SuppressWarnings("unchecked")	
	public static void prepareForUpdate(DocumentReference documentReference, String jsonString) { 
		try {
			LinkedTreeMap<String, Object> objT = StringUtility.fromJSON(jsonString, LinkedTreeMap.class);
			UpdateBodyDTO obj = StringUtility.fromJSON(StringUtility.toJSON(objT.get("body")), UpdateBodyDTO.class);
			
			//Category
			documentReference.getCategory().clear();
			if(StringUtils.isNotEmpty(obj.getTipoDocumentoLivAlto())) {
				documentReference.getCategory().add(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/media-category", obj.getTipoDocumentoLivAlto() , null)));
			}
			
			DocumentReferenceContextComponent drcc = documentReference.getContext();
			//Facility Type Code
			Coding codeFT = new Coding("urn:oid", obj.getTipologiaStruttura(), null);
			CodeableConcept ccFacilityType = new CodeableConcept(codeFT);
			drcc.setFacilityType(ccFacilityType);
			
			//Events
			List<CodeableConcept> events = new ArrayList<>();
			if(obj.getAttiCliniciRegoleAccesso()!=null) {
				for(String tipoDocLivAlto : obj.getAttiCliniciRegoleAccesso()) {
					events.add(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/media-category", tipoDocLivAlto , null)));
				}
				drcc.setEvent(events);
			}
			
			//Practice Setting
			drcc.setPracticeSetting(new CodeableConcept(new Coding("urn:oid", obj.getAssettoOrganizzativo(), null)));
			
			//Period
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Period period = new Period();
			if(obj.getDataInizioPrestazione() != null) {
				period.setStart(sdf.parse(obj.getDataInizioPrestazione()));
			}
			
			if(obj.getDataFinePrestazione() != null) {
				period.setEnd(sdf.parse(obj.getDataFinePrestazione()));
			}
			drcc.setPeriod(period);
		} catch(Exception ex) {
			log.error("Error while perform prepare for update : " , ex);
			throw new BusinessException("Error while perform prepare for update : " , ex);
		}
		  
	}

	private static void setDeletionRequest(BundleEntryComponent entry) {
		BundleEntryRequestComponent request = new BundleEntryRequestComponent();
		request.setMethod(HTTPVerb.DELETE);
		request.setUrl(getUrl(entry));
		entry.setRequest(request);
		entry.setFullUrl(null);
		entry.setSearch(null);
	}

	private static String getUrl(BundleEntryComponent entry) {
		IdType idType = entry.getResource().getIdElement();
		return idType.getResourceType() + "/" + idType.getIdPart();
	}

    private static void setRelatedDocumentReference(Bundle bundle, DocumentReference documentReference) {
    	String documentReferenceId = documentReference.getId();
    	BundleEntryComponent current = getBundleEntry(bundle, DocumentReference.class);
    	setRelatedDocumentReference(current.getResource(), documentReferenceId);
	}
    
    private static void setRelatedComposition(Bundle bundle, Bundle previousBundle) {
    	BundleEntryComponent current = getBundleEntry(bundle, Composition.class);
    	BundleEntryComponent previous = getBundleEntry(previousBundle, Composition.class);
    	setRelatedComposition(current.getResource(), previous.getResource().getId());
	}

	private static BundleEntryComponent getBundleEntry(Bundle bundle, Class<?> clazz) {
		return bundle
				.getEntry()
				.stream()
				.filter(entry -> (clazz.isInstance(entry.getResource())))
				.findFirst()
				.orElse(null);
	}

	private static void setRelatedDocumentReference(Resource documentReference, String previousIdentifier) {
		DocumentReference reference = (DocumentReference) documentReference;
		if (reference.getRelatesTo() == null) reference.setRelatesTo(new ArrayList<>());
		DocumentReferenceRelatesToComponent related = getRelatedDocumentReference(previousIdentifier);
		reference.getRelatesTo().add(related);
	}

	private static DocumentReferenceRelatesToComponent getRelatedDocumentReference(String previousIdentifier) {
		DocumentReferenceRelatesToComponent related = new DocumentReferenceRelatesToComponent();
		related.setCode(DocumentRelationshipType.REPLACES);
		related.setId(previousIdentifier);
		return related;
	}

	private static void setRelatedComposition(Resource compositionResource, String previousIdentifier) {
		Composition composition = (Composition) compositionResource;
		if (composition.getRelatesTo() == null) composition.setRelatesTo(new ArrayList<>());
		CompositionRelatesToComponent related = getRelatedComposition(previousIdentifier);
		composition.getRelatesTo().add(related);
	}

	private static CompositionRelatesToComponent getRelatedComposition(String previousIdentifier) {
		CompositionRelatesToComponent related = new CompositionRelatesToComponent();
		related.setCode(org.hl7.fhir.r4.model.Composition.DocumentRelationshipType.REPLACES);
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
