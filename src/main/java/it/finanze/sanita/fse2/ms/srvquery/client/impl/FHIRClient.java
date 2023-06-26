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
package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ResourceType;

import ca.uhn.fhir.rest.api.CacheControlDirective;
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

	public FHIRClient(final String serverURL, final String username, final String pwd) {
		client = FHIRR4Helper.createClient(serverURL, username, pwd);
	}

	public boolean create(final Bundle bundle) {
		try { 
			String id = transaction(bundle);
			return StringUtils.isNotEmpty(id);
		} 
		catch(BusinessException e) {
			throw e;
		}
		catch(Exception ex) {
			log.error("Errore while perform create client method: ", ex);
			throw new BusinessException("Errore while perform create client method : ", ex);
		}
	}
	
	public boolean delete(Bundle bundle) {
		try {
			String id = transaction(bundle);
			return StringUtils.isNotEmpty(id);
		} catch(Exception ex) {
			log.error("Errore while perform delete client method: ", ex);
			throw new BusinessException("Errore while perform delete client method : ", ex);
		}
	}
	
	public boolean replace(Bundle bundle) {
		try {
			String id = transaction(bundle);
			return StringUtils.isNotEmpty(id);
		} catch(Exception ex) {
			log.error("Errore while perform replace client method: ", ex);
			throw new BusinessException("Errore while perform replace client method:", ex);
		}
	}
	
 
	public String transaction(Bundle bundle) {
		String id = "";
		try {
			Bundle response = client.transaction().withBundle(bundle).execute();
			if(response!=null && StringUtils.isNotEmpty(response.getIdElement().getIdPart())) {
				id = response.getId();
			}
		}
		catch(Exception ex) {
			log.error("Error while perform transaction : " , ex);
			throw new BusinessException(ex.getMessage());
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
			esito = StringUtils.isNotEmpty(response.getId().toString());
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
	
	
	public CustomCapabilityStatement getServerCapabilities() {
		try {
			return client.capabilities()
					.ofType(CustomCapabilityStatement.class)
					.execute();
		} catch(Exception ex) {
			log.error("Errore while perform capabilities() client method:", ex);
			throw new BusinessException("Errore while perform capabilities() client method:", ex);
		}
		
	}
	
	
	private Parameters translateCodeOperation(Parameters params) {
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
		String searchParameter = StringUtility.getSearchParamFromMasterId(masterIdentifier);

		return client.search().forResource(DocumentReference.class).cacheControl(CacheControlDirective.noCache())
						.where(DocumentReference.IDENTIFIER.exactly().identifier(searchParameter)).returnBundle(Bundle.class).execute();
	}

}