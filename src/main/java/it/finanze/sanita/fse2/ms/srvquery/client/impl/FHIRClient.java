/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import static it.finanze.sanita.fse2.ms.srvquery.utility.OptionalUtility.getValue;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
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

	public boolean create(final Bundle bundle) {
		try { 
			String id = transaction(bundle);
			return !StringUtility.isNullOrEmpty(id);
		} catch(Exception ex) {
			log.error("Errore durante il salvataggio del bundle sul fhir server : ", ex);
			throw new BusinessException("Errore durante il salvataggio del bundle sul fhir server : ", ex);
		}
	}
	
	public boolean delete(Bundle bundle) {
		try {
			String id = transaction(bundle);
			return !StringUtility.isNullOrEmpty(id);
		} catch(Exception ex) {
			log.error("Errore durante la delete sul fhir server : ", ex);
			throw new BusinessException("Errore durante la delete sul fhir server : ", ex);
		}
	}
	
	public boolean replace(Bundle bundle) {
		try {
			String id = transaction(bundle);
			return !StringUtility.isNullOrEmpty(id);
		} catch(Exception ex) {
			log.error("Errore durante la delete sul fhir server : ", ex);
			throw new BusinessException("Errore durante la delete sul fhir server : ", ex);
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
 
	public Composition getComposition(DocumentReference documentReference) {
		try {
			return _getComposition(documentReference);
		} catch(Exception ex) {
			log.error("Errore durante la getComposition sul fhir server : ", ex);
			throw new BusinessException("Errore durante la getComposition sul fhir server : ", ex);
		}
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

	private String transaction(final Bundle bundle) {
		String id = "";
		Bundle response = client.transaction().withBundle(bundle).execute();
		if(response!=null && !StringUtility.isNullOrEmpty(response.getIdElement().getIdPart())) {
			id = response.getId();
		}
		return id;
	}
	 

	private boolean _update(DocumentReference documentReference) {
		if (documentReference == null) return false;
		MethodOutcome response = client
				.update()
				.resource(documentReference)
				.execute();
		return isExecuted(response);
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
	
	
	public Bundle getDocument(final String idComposition, final String url) {
		try {
			return (Bundle)client.search().byUrl(url+"/"+idComposition+"/$document").execute(); 
		} catch(Exception ex) {
			log.error("Errore durante la getDocument sul fhir server : ", ex);
			throw new BusinessException("Errore durante la getDocument sul fhir server : ", ex);
		}
	}
	
	private boolean isExecuted(MethodOutcome response) {
		return response.getOperationOutcome() == null;
	}

	private boolean isExecuted(Bundle response) {
		return response != null && response.hasEntry();
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
	
	
	public DocumentReference getDocumentReferenceBundle(final String masterIdentifier) {
		DocumentReference output = null;
		try {
			Bundle bundle = findByMasterIdentifier(masterIdentifier);
			if(bundle!=null && !bundle.getEntry().isEmpty()) {
				for(BundleEntryComponent entry : bundle.getEntry()) {
					if(ResourceType.DocumentReference.equals(entry.getResource().getResourceType())){
						output = (DocumentReference)entry.getResource();
						break;
					} 
				}
			}
		} catch(Exception ex) {
			log.error("Error while get document reference bundle : ", ex);
			throw new BusinessException("Error while get document reference bundle : ", ex);
		}
		return output;
	}
	
	public Bundle findByMasterIdentifier(final String masterIdentifier) {
		String searchParameter = StringUtility.getSearchParameterFromMasterIdentifier(masterIdentifier);
		
		return client.search().forResource(DocumentReference.class)
						.where(DocumentReference.IDENTIFIER.exactly().identifier(searchParameter)).returnBundle(Bundle.class).execute();
	}

}