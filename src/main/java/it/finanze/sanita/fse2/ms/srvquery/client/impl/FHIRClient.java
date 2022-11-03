/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.util.CollectionUtils;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Client Implementation 
 *
 */
@Slf4j
public class FHIRClient {

	private IGenericClient client;

	public FHIRClient(String serverURL) {
		client = FHIRR4Helper.createClient(serverURL);
	}

	public Bundle saveBundleWithTransaction(Bundle bundle) {
		return client.transaction().withBundle(bundle).execute();
	}

	public boolean read(final String masterIdentifier) {
		boolean output = false;
		try {
			Bundle bundle = client.search().forResource(DocumentReference.class).where(DocumentReference.IDENTIFIER.exactly().identifier(masterIdentifier))
					.returnBundle(Bundle.class).execute();
			output = !CollectionUtils.isEmpty(bundle.getEntry());
		} catch(Exception ex) {
			log.error("Errore durante la read sul fhir server : ", ex);
			throw new BusinessException("Errore durante la read sul fhir server : ", ex);
		}
		return output;
	}

	public String translateCode(String code, String system, String targetSystem){

		try {

			Parameters inParams = new Parameters();
			inParams.addParameter().setName("code").setValue(new StringType(code));
			inParams.addParameter().setName("system").setValue(new StringType(system));
			inParams.addParameter().setName("targetSystem").setValue(new StringType(targetSystem));

			Class<?> conceptMapClass = client.getFhirContext().getResourceDefinition("ConceptMap").getImplementingClass();

			Parameters outParams = client
					.operation()
					.onType((Class<? extends IBaseResource>) conceptMapClass)
					.named("$translate")
					.withParameters(inParams)
					.useHttpGet()
					.execute();



			if(outParams.getParameter().get(2)!=null && outParams.getParameter().get(2).getName().equals("match")){
				return extractCodeFromParams(outParams);
			} else {
				return "";
			}

		} catch (Exception e) {
			log.error("Error translating code " + code, e);
			//TODO: lanciare eccezione custom
			throw new BusinessException(e);
		}
	}


	private String extractCodeFromParams(Parameters outParams) {
		return outParams.getParameter().get(2).getPart().get(0).getValue().getNamedProperty("code").getValues().get(0).toString();
	}

	// TODO: delete https://hapifhir.io/hapi-fhir/docs/client/generic_client.html
	// ricevo il masteridentifier con cui recupero la DocumentReference 


	// public Boolean delete(String identifier){
	// 	Boolean res = false;
	// 	try {
	// 		MethodOutcome response = client
	// 		.delete()
	// 		.resourceById(new IdType("Patient", identifier))
	// 		.execute();

	// 		// outcome may be null if the server didn't return one
	// 		OperationOutcome outcome = (OperationOutcome) response.getOperationOutcome();
	// 		if (outcome != null) {
	// 			res = true;
	// 		}
	// 	} catch (Exception e) {
	// 		// TODO: handle exception
	// 	}
	// 	return res;
	// }

	
	
}