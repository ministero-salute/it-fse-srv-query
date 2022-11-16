/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import static it.finanze.sanita.fse2.ms.srvquery.utility.OptionalUtility.getValue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.OptionalUtility;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Client Implementation 
 */
@Slf4j
public class FHIRClient {

	// DOC: https://hapifhir.io/hapi-fhir/docs/client/generic_client.html
	
	private IGenericClient client;

	public FHIRClient(final String serverURL) {
		client = FHIRR4Helper.createClient(serverURL);
	}

	public Bundle create(Bundle bundle) {
		try { 
			return _create(bundle);
		} catch(Exception ex) {
			log.error("Errore durante il salvataggio del bundle sul fhir server : ", ex);
			throw new BusinessException("Errore durante il salvataggio del bundle sul fhir server : ", ex);
		}
	}

	public boolean deleteResource(IdType idType) {
		try {
			return _deleteResource(idType);
		} catch(Exception ex) {
			log.error("Errore durante la deleteResoruce sul fhir server : ", ex);
			throw new BusinessException("Errore durante la deleteResoruce sul fhir server : ", ex);
		}
	}
	
	public boolean update(DocumentReference documentReference) {
		try {
			return _update(documentReference);
		} catch(Exception ex) {
			log.error("Errore durante la deleteResoruce sul fhir server : ", ex);
			throw new BusinessException("Errore durante la deleteResoruce sul fhir server : ", ex);
		}
	}

	public DocumentReference getDocumentReference(final String identifier) {
		try {
			return _getDocumentReference(identifier);
		} catch(Exception ex) {
			log.error("Errore durante la getDocumentReference sul fhir server : ", ex);
			throw new BusinessException("Errore durante la getDocumentReference sul fhir server : ", ex);
		}
	}
	
	public Composition getComposition(DocumentReference documentReference) {
		try {
			return _getComposition(documentReference);
		} catch(Exception ex) {
			log.error("Errore durante la getComposition sul fhir server : ", ex);
			throw new BusinessException("Errore durante la getComposition sul fhir server : ", ex);
		}
	}

	public Bundle getDocument(Composition composition) {
		try {
			return _getDocument(composition);
		} catch(Exception ex) {
			log.error("Errore durante la getDocument sul fhir server : ", ex);
			throw new BusinessException("Errore durante la getDocument sul fhir server : ", ex);
		}
	}
	
	public boolean checkExists(final String masterIdentifier) {
		return getDocumentReference(masterIdentifier) != null;
	}

	public String translateCode(String code, String system, String targetSystem) {
		try {
			return _translateCode(code, system, targetSystem);
		} catch (Exception ex) {
			log.error("Errore durante la translation del code " + code);
			throw new BusinessException("Errore durante la translation del code " + code);
		}
	}

	private Composition _getCompositionById(final String resourceId) {
		return client
				.read()
				.resource(Composition.class)
				.withId(resourceId)
				.execute();
	}

	private Bundle _create(Bundle bundle) {
		if (bundle == null) return null;
		return client
				.transaction()
				.withBundle(bundle)
				.execute();
	}

	private boolean _deleteResource(IdType idType) {
		if (idType == null) return false;
		MethodOutcome response = client
				.delete()
				.resourceById(idType)
				.execute();
		return isExecuted(response);
	}

	private boolean _update(DocumentReference documentReference) {
		if (documentReference == null) return false;
		MethodOutcome response = client
				.update()
				.resource(documentReference)
				.execute();
		return isExecuted(response);
	}

	private DocumentReference _getDocumentReference(final String identifier) {
		return getDocumentReferenceBundle(identifier)
				.getEntry()
				.stream()
				.map(entry -> entry.getResource())
				.filter(resource -> resource instanceof DocumentReference)
				.map(resource -> (DocumentReference) resource)
				.findFirst()
				.orElse(null);
	}
	
	private Bundle getDocumentReferenceBundle(String masterIdentifier) {
		Bundle bundle = _getDocumentReferenceBundle(masterIdentifier);
		List<BundleEntryComponent> entry = OptionalUtility.getValue(bundle, Bundle::getEntry);
		if (isEmpty(entry)) return null;
		return bundle;
	}

	private Bundle _getDocumentReferenceBundle(String masterIdentifier) {
		if (isBlank(masterIdentifier)) return null;
		return client
				.search()
				.forResource(DocumentReference.class)
				.where(DocumentReference.IDENTIFIER.exactly().identifier(masterIdentifier))
				.returnBundle(Bundle.class)
				.execute();
	}

	private Composition _getComposition(DocumentReference documentReference) {
    	DocumentReferenceContextComponent context = getValue(documentReference, DocumentReference::getContext);
    	if (context == null) return null;
    	if (isEmpty(context.getRelated())) return null;
    	return _getComposition(context.getRelated());
	}
	
	private Composition _getComposition(List<Reference> references) {
		if (isEmpty(references)) return null;
		return references
    			.stream()
    			.filter(Objects::nonNull)
    			.map(Reference::getReference)
    			.map(this::_getCompositionById)
    			.findFirst()
    			.orElse(null);
	}
	
	private Bundle _getDocument(Composition composition) {
		String compositionId = getValue(composition, Composition::getId);
		if (compositionId == null) return null;
		return _getDocument(compositionId);
	}
	
	private Bundle _getDocument(String compositionId) {
		return client
				.operation()
				.onInstance(compositionId)
				.named("$document")
				.withNoParameters(Parameters.class)
				.returnResourceType(Bundle.class)
				.execute();
	}
	
	private boolean isExecuted(MethodOutcome response) {
		return response.getOperationOutcome() == null;
	}

	public String _translateCode(String code, String system, String targetSystem) {
		Parameters inParams = new Parameters();
		inParams.addParameter().setName("code").setValue(new StringType(code));
		inParams.addParameter().setName("system").setValue(new StringType(system));
		inParams.addParameter().setName("targetSystem").setValue(new StringType(targetSystem));
		Parameters outParams = _translateCode(inParams);
		return extractCodeFromParams(outParams);
	}
	
	private Parameters _translateCode(Parameters params) {
//		Class<?> conceptMapClass = client.getFhirContext().getResourceDefinition("ConceptMap").getImplementingClass();
		return client
				.operation()
//				.onType((Class<? extends IBaseResource>) conceptMapClass)
				.onType(ConceptMap.class)
				.named("$translate")
				.withParameters(params)
				.useHttpGet()
				.execute();
	}
	
	private String extractCodeFromParams(Parameters outParams) {
		return outParams
				.getParameter()
				.stream()
				.filter(param -> param.getName().equals("match"))
				.findFirst()
				.map(param -> extractCodeFromParam(param))
				.orElse(null);
	}

	private String extractCodeFromParam(ParametersParameterComponent param) {
		//TODO
		return param.getPart().get(0).getValue().getNamedProperty("code").getValues().get(0).toString();
	}

}