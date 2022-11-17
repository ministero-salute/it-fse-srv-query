/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
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

	
	private IGenericClient client;

	public FHIRClient(final String serverURL) {
		client = FHIRR4Helper.createClient(serverURL);
	}

	public boolean create(final Bundle bundle) {
		try { 
			String id = transaction(bundle);
			return !StringUtility.isNullOrEmpty(id);
		} catch(Exception ex) {
			log.error("Errore while perform create client method: ", ex);
			throw new BusinessException("Errore while perform create client method : ", ex);
		}
	}
	
	public boolean delete(Bundle bundle) {
		try {
			String id = transaction(bundle);
			return !StringUtility.isNullOrEmpty(id);
		} catch(Exception ex) {
			log.error("Errore while perform delete client method: ", ex);
			throw new BusinessException("Errore while perform delete client method : ", ex);
		}
	}
	
	public boolean replace(Bundle bundle) {
		try {
			String id = transaction(bundle);
			return !StringUtility.isNullOrEmpty(id);
		} catch(Exception ex) {
			log.error("Errore while perform replace client method: ", ex);
			throw new BusinessException("Errore while perform replace client method:", ex);
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

	private String transaction(final Bundle bundle) {
		String id = "";
		try {
			Bundle response = client.transaction().withBundle(bundle).execute();
			if(response!=null && !StringUtility.isNullOrEmpty(response.getIdElement().getIdPart())) {
				id = response.getId();
			}
		} catch(Exception ex) {
			log.error("Error while perform transaction : " , ex);
			throw new BusinessException("Error while perform transaction : " , ex);
		}
		return id;
	}
	 
	public boolean update(final DocumentReference documentReference) {
		boolean esito = false;
		try {
			MethodOutcome response = client
					.update()
					.resource(documentReference)
					.execute();
			esito = response.getCreated();
		} catch(Exception ex) {
			log.error("Errore while perform update client method:" , ex);
			throw new BusinessException("Errore while perform update client method:" , ex);
		}
		return esito;
	}
   
	
	public Bundle getDocument(final String idComposition, final String url) {
		try {
			return (Bundle)client.search().byUrl(url+"/"+idComposition+"/$document").execute(); 
		} catch(Exception ex) {
			log.error("Errore while perform getDocument client method:", ex);
			throw new BusinessException("Errore while perform getDocument client method:", ex);
		}
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
			log.error("Errore while perform getDocumentReferenceBundle client method:", ex);
			throw new BusinessException("Errore while perform getDocumentReferenceBundle client method:", ex);
		}
		return output;
	}
	
	public Bundle findByMasterIdentifier(final String masterIdentifier) {
		String searchParameter = StringUtility.getSearchParameterFromMasterIdentifier(masterIdentifier);
		
		return client.search().forResource(DocumentReference.class)
						.where(DocumentReference.IDENTIFIER.exactly().identifier(searchParameter)).returnBundle(Bundle.class).execute();
	}

}