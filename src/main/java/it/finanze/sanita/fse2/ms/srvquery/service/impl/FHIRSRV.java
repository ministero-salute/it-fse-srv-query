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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.SearchParameter;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FhirCFG;
import it.finanze.sanita.fse2.ms.srvquery.crypt.CryptUtility;
import it.finanze.sanita.fse2.ms.srvquery.dto.ResourceSearchParameterDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * FHIR Service Implementation
 */
@Service
@Slf4j
public class FHIRSRV implements IFHIRSRV {

    @Autowired
    private FhirCFG fhirCFG;

    @Autowired
    private FHIRClient fhirClient;

    @Autowired
    FHIRUtility fhirUtility;

    @Override
    public boolean create(final FhirPublicationDTO createDTO) {
        boolean esito = false;
        try {
            String json = createDTO.getJsonString();
            log.debug("FHIR bundle: {}", json);
            Bundle bundle = fhirUtility.deserializeBundle(json);
            CryptUtility.encryptPatientInfo(bundle);
            esito = fhirClient.create(bundle, createDTO.getRegion());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Error while perform create operation :", ex);
            throw new BusinessException("Error while perform create operation :", ex);
        }
        return esito;
    }

    @Override
    public boolean delete(final String masterIdentifier, String region) {
        boolean output = false;
        try {

            DocumentReference documentReference = fhirClient.getDocumentReferenceBundle(masterIdentifier, region);
            if (documentReference != null) {
                String idComposition = documentReference.getContext().getRelated().get(0).getReference();
                Bundle bundleToDelete = fhirClient.getDocument(idComposition, fhirCFG.getFhirServerUrl(), region);
                fhirUtility.prepareForDelete(bundleToDelete, documentReference);
                output = fhirClient.delete(bundleToDelete, region);
            }

        } catch (Exception ex) {
            log.error("Error while perform delete operation : ", ex);
            throw new BusinessException("Error while perform delete operation : ", ex);
        }
        return output;
    }

    @Override
    public boolean replace(final FhirPublicationDTO body) {
        boolean output = false;
        try {
            Bundle bundleToReplace = fhirUtility.deserializeBundle(body.getJsonString());
            String identifier = body.getIdentifier();
            String region = body.getRegion();
            DocumentReference documentReference = fhirClient.getDocumentReferenceBundle(identifier, region);
            String idComposition = documentReference.getContext().getRelated().get(0).getReference();
            Bundle previousBundle = fhirClient.getDocument(idComposition, fhirCFG.getFhirServerUrl(), region);
            fhirUtility.prepareForReplace(bundleToReplace, documentReference, previousBundle);
            output = fhirClient.replace(bundleToReplace, body.getRegion());
        } catch (Exception ex) {
            log.error("Error while perform replace operation : ", ex);
            throw new BusinessException("Error while perform replace operation : ", ex);
        }
        return output;
    }

    @Override
    public boolean updateMetadata(final FhirPublicationDTO body) {
        boolean output = false;
        try {
            String identifier = body.getIdentifier();
            DocumentReference documentReference = fhirClient.getDocumentReferenceBundle(identifier, body.getRegion());
            fhirUtility.prepareForUpdate(documentReference, body.getJsonString());
            output = fhirClient.update(documentReference, body.getRegion());
        } catch (Exception ex) {
            log.error("Error while perform update operation : ", ex);
            throw new BusinessException("Error while perform update operation : ", ex);
        }
        return output;
    }

    @Override
    public boolean checkExists(String masterIdentifier, String region) {
        boolean isFound = true;

        if (StringUtils.isEmpty(masterIdentifier)) {
            throw new BusinessException("Attenzione. Il master identifier risulta essere null");
        }

        Bundle bundle = fhirClient.findByMasterIdentifier(masterIdentifier, region);
        if (bundle == null || bundle.getEntry().isEmpty()) {
            isFound = false;
        }

        return isFound;
    }

    private List<String> parametersFromPaths(List<StringType> paths) {
        if (paths == null)
            paths = new ArrayList<>();
        return paths
                .stream()
                .map(PrimitiveType::asStringValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceSearchParameterDTO> getResourcesSearchParameters() {

        Map<String, List<String>> paramsByResource = new HashMap<>();

        Bundle bundle = fhirClient.getSearchParams();
        log.info("Retrieved {} search parameters from FHIR Server", bundle.getTotal());

        // For every Search Param in the response
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof SearchParameter) {
                SearchParameter searchParam = (SearchParameter) entry.getResource();
                // Expression is the path in the Referent Resource like Observation.note.author
                String expression = searchParam.getExpression();

                for (CodeType base : searchParam.getBase()) {
                    String resourceName = base.getValue(); // e.g. "Observation"
                    paramsByResource
                            .computeIfAbsent(resourceName, rn -> new ArrayList<>())
                            .add(expression);
                }

            }
        }

        List<ResourceSearchParameterDTO> output = new ArrayList<>(paramsByResource.size());
        for (Map.Entry<String, List<String>> e : paramsByResource.entrySet()) {
            String resourceName = e.getKey();
            List<String> expressions = e.getValue();
            // Optionally sort or dedupe expressions
            List<String> uniqueExpr = expressions.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            output.add(new ResourceSearchParameterDTO(resourceName, uniqueExpr));
        }

        return output;
    }

}
