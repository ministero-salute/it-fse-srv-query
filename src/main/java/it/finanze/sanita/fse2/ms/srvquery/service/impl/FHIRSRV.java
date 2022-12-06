/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility.deserializeBundle;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.UnknownException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.ProfileUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Service Implementation 
 */
@Service
@Slf4j
public class FHIRSRV implements IFHIRSRV {


	@Autowired
	private FhirCFG fhirCFG;

	private FHIRClient fhirClient;

	@Autowired
	private ProfileUtility profileUtility;

	@Async
	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		fhirClient = new FHIRClient(fhirCFG.getFhirServerUrl(),fhirCFG.getFhirServerUser() ,fhirCFG.getFhirServerPwd());
	}

	@Override
	public boolean create(final FhirPublicationDTO createDTO) {
		boolean esito = false;
		try {
			if(fhirClient==null) {
				initialize();
			}

			String json = createDTO.getJsonString();
			log.debug("FHIR bundle: {}", json);
			Bundle bundle = deserializeBundle(json);
			if(profileUtility.isDevProfile()) {
				for(BundleEntryComponent entry : bundle.getEntry()) {
					if(ResourceType.DocumentReference.equals(entry.getResource().getResourceType())){
						DocumentReference documentReference = (DocumentReference) entry.getResource();
						if (documentReference.getMasterIdentifier().getValue().contains("UNKONOWN_EDS")) {
							throw new UnknownException("Eccezione di test");
						} 
					}
				}
			}
			esito = fhirClient.create(bundle);
		} catch (UnknownException e) {
			throw e;
		} catch(Exception ex) {
			log.error("Error while perform create operation :", ex);
			throw new BusinessException("Error while perform create operation :", ex);
		}
		return esito;
	}

	@Override
	public boolean delete(final String masterIdentifier) {
		boolean output = false;
		try {
			if(fhirClient==null) {
				initialize();
			}

			DocumentReference documentReference = fhirClient.getDocumentReferenceBundle(masterIdentifier);
			if(documentReference!=null) {
				String idComposition = documentReference.getContext().getRelated().get(0).getReference();
				Bundle bundleToDelete = fhirClient.getDocument(idComposition,fhirCFG.getFhirServerUrl());
				FHIRUtility.prepareForDelete(bundleToDelete, documentReference);
				output = fhirClient.delete(bundleToDelete);
			}
		} catch(Exception ex) {
			log.error("Error while perform delete operation : " , ex);
			throw new BusinessException("Error while perform delete operation : " , ex);
		}
		return output;
	}

	@Override
	public boolean replace(final FhirPublicationDTO body) {
		boolean output = false;
		try {
			if(fhirClient==null) {
				initialize();
			}

			Bundle bundleToReplace = deserializeBundle(body.getJsonString());
			String identifier = body.getIdentifier();
			DocumentReference documentReference = fhirClient.getDocumentReferenceBundle(identifier);
			String idComposition = documentReference.getContext().getRelated().get(0).getReference();
			Bundle bundle = fhirClient.getDocument(idComposition,fhirCFG.getFhirServerUrl());
			FHIRUtility.prepareForReplace(bundleToReplace, documentReference, bundle);
			output = fhirClient.replace(bundleToReplace);
		} catch(Exception ex) {
			log.error("Error while perform replace operation : " , ex);
			throw new BusinessException("Error while perform replace operation : " , ex);
		}
		return output;
	}

	@Override
	public boolean updateMetadata(final FhirPublicationDTO body) {
		boolean output = false;
		try {
			if(fhirClient==null) {
				initialize();
			}

			String identifier = body.getIdentifier();
			DocumentReference documentReference = fhirClient.getDocumentReferenceBundle(identifier);
			FHIRUtility.prepareForUpdate(documentReference, body.getJsonString());
			output = fhirClient.update(documentReference);
		} catch(Exception ex) {
			log.error("Error while perform update operation : " , ex);
			throw new BusinessException("Error while perform update operation : " , ex);
		}
		return output;
	}

	@Override
	public boolean checkExists(final String masterIdentifier) {
		boolean isFound = true;
		if(StringUtility.isNullOrEmpty(masterIdentifier)) {
			throw new BusinessException("Attenzione. Il master identifier risulta essere null");
		}

		if(fhirClient==null) {
			initialize();
		}

		Bundle bundle = fhirClient.findByMasterIdentifier(masterIdentifier);
		if(bundle==null || bundle.getEntry().isEmpty()) {
			isFound = false;
		}

		return isFound;
	}

	@Override
	public String translateCode(String code, String system, String targetSystem) {
		if(fhirClient==null) {
			initialize();
		}
		String out = fhirClient.translateCode(code, system, targetSystem);
		log.info("Code translated result: {}", out);
		return out;
	}

}
