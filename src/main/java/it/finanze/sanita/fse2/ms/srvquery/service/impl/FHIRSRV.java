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
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility.deserializeBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.CustomCapabilityStatement;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.ResourceSearchParameterDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.FileUtility;
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

	@Async
	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		fhirClient = new FHIRClient(fhirCFG.getFhirServerUrl(), fhirCFG.getFhirServerUser(), fhirCFG.getFhirServerPwd());
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
			esito = fhirClient.create(bundle);
		} catch(BusinessException e) {
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
				Bundle bundleToDelete = fhirClient.getDocument(idComposition, fhirCFG.getFhirServerUrl());
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
			Bundle previousBundle = fhirClient.getDocument(idComposition,fhirCFG.getFhirServerUrl());
			FHIRUtility.prepareForReplace(bundleToReplace, documentReference, previousBundle);
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
		if(StringUtils.isEmpty(masterIdentifier)) {
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
 
	private List<String> parametersFromPaths(List<StringType> paths) {
		if (paths == null) paths = new ArrayList<>();
		return paths
			.stream()
			.map(PrimitiveType::asStringValue)
			.collect(Collectors.toList());
	}
	
	@Override
	public List<ResourceSearchParameterDTO> getResourcesSearchParameters() {
		CustomCapabilityStatement capabilities = fhirClient.getServerCapabilities();
		return capabilities.getResourceSearchPaths()
			.stream()
			.map(rp -> new ResourceSearchParameterDTO(rp.getType(), parametersFromPaths(rp.getSearchPath())) )
			.collect(Collectors.toList()); 
	}

	
	public static void main(String[] args) {
		Map<String,String> placeholderMap = getMapPlaceholder();
		String bundleLDO = new String(FileUtility.getFileFromInternalResources("BundleLDO.json"));
		System.out.println(bundleLDO);
		bundleLDO = replacePlaceholderBundle(bundleLDO,placeholderMap);
		System.out.println(bundleLDO);
	}
	
	private static Map<String,String> getMapPlaceholder(){
		Map<String,String> out = new HashMap<>();
		out.put("####PLACEHOLDER_IDBUNDLE####", UUID.randomUUID().toString());
		out.put("####ID1###", UUID.randomUUID().toString());
		out.put("####ID2###", UUID.randomUUID().toString());
		out.put("####ID3###", UUID.randomUUID().toString());
		out.put("####ID4###", UUID.randomUUID().toString());
		out.put("####ID5###", UUID.randomUUID().toString());
		out.put("####ID6###", UUID.randomUUID().toString());
		out.put("####ID7###", UUID.randomUUID().toString());
		out.put("####ID8###", UUID.randomUUID().toString());
		out.put("####ID9###", UUID.randomUUID().toString());
		out.put("####ID10###", UUID.randomUUID().toString());
		out.put("####ID11###", UUID.randomUUID().toString());
		out.put("####ID12###", UUID.randomUUID().toString());
		out.put("####ID13###", UUID.randomUUID().toString());
		out.put("####ID14###", UUID.randomUUID().toString());
		out.put("####ID15###", UUID.randomUUID().toString());
		out.put("####ID16###", UUID.randomUUID().toString());
		out.put("####ID17###", UUID.randomUUID().toString());
		out.put("####ID18###", UUID.randomUUID().toString());
		out.put("####ID19###", UUID.randomUUID().toString());
		out.put("####ID20###", UUID.randomUUID().toString());
		out.put("####ID21###", UUID.randomUUID().toString());
		out.put("####ID22###", UUID.randomUUID().toString());
		out.put("####ID23###", UUID.randomUUID().toString());
		out.put("####ID24###", UUID.randomUUID().toString());
		out.put("####ID25###", UUID.randomUUID().toString());
		out.put("####ID26###", UUID.randomUUID().toString());
		out.put("####ID27###", UUID.randomUUID().toString());
		out.put("####ID28###", UUID.randomUUID().toString());
		return out;
	}
	
	private static String replacePlaceholderBundle(String bundle,Map<String,String> placeholderMap) {
		for(Entry<String, String> entry : placeholderMap.entrySet()) {
			bundle = bundle.replace(entry.getKey(), entry.getValue());
		}
		return bundle;
	}
}
