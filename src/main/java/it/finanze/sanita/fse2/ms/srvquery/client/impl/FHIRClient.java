/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.IQuery;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.UriClientParam;
import ca.uhn.fhir.util.ParametersUtil;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.ValidateCodeResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

import static org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode.*;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus.*;

/** 
 * FHIR Client Implementation 
 */
@Slf4j
public class FHIRClient {

	private IGenericClient client;
	
	private String srvURL;

	public FHIRClient(final String serverURL, final String username, final String pwd) {
		client = FHIRR4Helper.createClient(serverURL, username, pwd);
	}

	public FHIRClient(final String serverURL) {
		srvURL = serverURL;
		client = FHIRR4Helper.createClient(serverURL);
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

/*
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

*/
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

/*
	///////////////////////////////////////////////////////////////////////////////////////////////
	// READ METADATA RESOURCE
	///////////////////////////////////////////////////////////////////////////////////////////////

	private <T> T read(String id, Class<? extends MetadataResource> mr) {
		try {
			return (T) client.read().resource(mr).withId(id).execute();
		} catch(Exception ex) {
			log.error("Errore while perform read client method:", ex);
			throw new BusinessException("Errore while perform read client method:", ex);
		}
	}

	public <T> T readVS(String id) {
		return read(id, ValueSet.class);
	}

	public <T> T readCS(String id) {
		return read(id, CodeSystem.class);
	}

	public <T> T readCM(String id) {
		return read(id, ConceptMap.class);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// SEARCH METADATA RESOURCE
	///////////////////////////////////////////////////////////////////////////////////////////////

	private <T> List<T> searchActive(Class<? extends MetadataResource> mr) {
		List<T> out = new ArrayList<>();
		try {
			Bundle bundle = client.search().forResource(mr).cacheControl(CacheControlDirective.noCache())
					.where(CodeSystem.STATUS.exactly().identifier("active")).returnBundle(Bundle.class).execute();
			for (BundleEntryComponent bec:bundle.getEntry()) {
				T cs= (T) bec.getResource();
				out.add(cs);
			}
			return out;
		} catch(Exception ex) {
			log.error("Errore while perform searchActive client method:", ex);
			throw new BusinessException("Errore while perform searchActive client method:", ex);
		}
	}

	public List<ValueSet> searchActiveValueSet() {
		return searchActive(ValueSet.class);
	}

	public List<CodeSystem> searchActiveCodeSystem() {
		return searchActive(CodeSystem.class);
	}

	private <T> List<T> searchModified(Date start, Class<? extends MetadataResource> mr) {
		List<T> out = new ArrayList<>();
		try {
			// Search for any resource, disabling cache
			IQuery<IBaseBundle> query = client.search().forResource(mr).cacheControl(CacheControlDirective.noCache());
			// If startDate is provided, use it otherwise returns any resource
			if(start != null) {
				// Include active and not active resources
				query = query.where(
					new DateClientParam("_lastUpdate").after().millis(start)
				);
			} else {
				// Exclude all not-active resources
				query = query.where(CodeSystem.STATUS.exactly().identifier("active"));
			}
			Bundle bundle = query.returnBundle(Bundle.class).execute();
			for (BundleEntryComponent bec:bundle.getEntry()) {
				T cs= (T) bec.getResource();
				out.add(cs);
			}
			return out;
		} catch(Exception ex) {
			log.error("Errore while perform searchModified client method:", ex);
			throw new BusinessException("Errore while perform searchModified client method:", ex);
		}
	}

	public List<ValueSet> searchModifiedValueSet(Date start) {
		return searchModified(start, ValueSet.class);
	}

	public List<CodeSystem> searchModifiedCodeSystem(Date start) {
		return searchModified(start, CodeSystem.class);
	}

	public List<ConceptMap> searchConceptMapBySourceSystem(MetadataResource mr) {
		List<ConceptMap> out = new ArrayList<>();

		if (mr!=null) {
			String sourceSystem = mr.getId().split("/_history")[0];

			Bundle bundle = client.search()
					.forResource(ConceptMap.class)
					.where(new UriClientParam("source-system").matches().value(sourceSystem))
					.returnBundle(Bundle.class)
					.execute();

			for (BundleEntryComponent entry : bundle.getEntry()) {
				if (entry.getResource() instanceof ConceptMap) {
					ConceptMap conceptMap = (ConceptMap) entry.getResource();
					out.add(conceptMap);
				}
			}
		}
		return out;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// INSERT
	///////////////////////////////////////////////////////////////////////////////////////////////

	public String insertCS(String name, List<CodeDTO> codes) {
		return insertCS(null, ACTIVE, COMPLETE, name, null, codes);
	}

	public void updateCS(String id, List<CodeDTO> append) {
		CodeSystem cs = client.read().resource(CodeSystem.class).withId(id).execute();
		if(cs == null) throw new IllegalArgumentException(String.format("The CS with id %s doesn't exists", id));
		for (CodeDTO code: append) {
			ConceptDefinitionComponent cdc = new ConceptDefinitionComponent();
			cdc.setDisplay(code.getDisplay());
			cdc.setCode(code.getCode());
			cs.getConcept().add(cdc);
		}
		client.update().resource(cs).execute();
	}

	private String insertCS(final String id, final PublicationStatus ps, final CodeSystemContentMode cscm, String name, String version, List<CodeDTO> codes) {
		CodeSystem codeSystem = new CodeSystem();
		codeSystem.setId(id);
		codeSystem.setName(name);
		String ver = "1.0.0";
		if (version!=null && version.length()>0 ) {
			ver = version;
		}
		codeSystem.setVersion(ver);
		codeSystem.setStatus(ps);
		codeSystem.setContent(cscm);

		List<ConceptDefinitionComponent> concepts = new ArrayList<>();

		for (CodeDTO code:codes) {
			ConceptDefinitionComponent cdc = new ConceptDefinitionComponent();
			cdc.setDisplay(code.getDisplay());
			cdc.setCode(code.getCode());
			concepts.add(cdc);
		}

		codeSystem.setConcept(concepts);
		codeSystem.setDate(new Date());

		Bundle transactionBundle = new Bundle();
		transactionBundle.setType(Bundle.BundleType.TRANSACTION);
		Bundle.BundleEntryComponent codeSystemEntry = new Bundle.BundleEntryComponent();
		codeSystemEntry.setResource(codeSystem).getRequest().setMethod(HTTPVerb.POST).setUrl(codeSystem.getUrl());
		transactionBundle.addEntry(codeSystemEntry);

		Bundle response = client.transaction().withBundle(transactionBundle).execute();
		return response.getEntryFirstRep().getResponse().getLocation().split("/")[1];
	}

	public String insertVS(String name, final String url, Map<MetadataResource, List<CodeDTO>> codes) {
		Map<String, List<CodeDTO>> codesTxt = new HashMap<>();
		for (Entry<MetadataResource, List<CodeDTO>> entry:codes.entrySet()) {
			String system = entry.getKey().getId().split("/_history")[0];
			codesTxt.put(system, entry.getValue());
		}
		String tmpUrl = url;
		if (tmpUrl==null || tmpUrl.isEmpty()) {
			tmpUrl = srvURL + "/ValueSet/" + UUID.randomUUID().toString();
		}

		return insertVS(null, tmpUrl, ACTIVE, name, null, codesTxt);
	}

	private String insertVS(final String id, final String url, final PublicationStatus ps, String name, String version, Map<String, List<CodeDTO>> codes) {
		ValueSet valueSet = new ValueSet();
		valueSet.setId(id);
		valueSet.setName(name);
		valueSet.setUrl(url);
		String ver = "1.0.0";
		if (version!=null && version.length()>0 ) {
			ver = version;
		}
		valueSet.setVersion(ver);
		valueSet.setStatus(ps);
		valueSet.setDate(new Date());

		ValueSetComposeComponent compose = new ValueSetComposeComponent();

		for (Entry<String, List<CodeDTO>> entryCodes:codes.entrySet()) {
			String system = entryCodes.getKey();
			ConceptSetComponent csc = new ConceptSetComponent();
			csc.setSystem(system);
			for (CodeDTO systemCode:entryCodes.getValue()) {
				ConceptReferenceComponent concept = new ConceptReferenceComponent();
				concept.setCode(systemCode.getCode());
				concept.setDisplay(systemCode.getDisplay());

				csc.addConcept(concept);
			}
			compose.addInclude(csc);
		}
		valueSet.setCompose(compose);

		List<IBaseResource> resourceList = new ArrayList<>();
		resourceList.add(valueSet);
		ArrayList<IBaseResource> resources = (ArrayList) client.transaction().withResources(resourceList).execute();
		String out = ((ValueSet)resources.get(0)).getId();
		return out.split("/_history")[0];
	}

	public String insertCM(String name, final String url, MetadataResource mrSource, MetadataResource mrTarget, Map<String, String> sourceToTargetCodes) {

		ConceptMap conceptMap = new ConceptMap();

		String tmpUrl = url;
		if (tmpUrl==null || tmpUrl.isEmpty()) {
			tmpUrl = srvURL + "ConceptMap/" + UUID.randomUUID().toString();
		}

		conceptMap.setUrl(tmpUrl);

		String systemSource = mrSource.getId().split("/_history")[0];
		String systemTarget = mrTarget.getId().split("/_history")[0];

		// set source and target uri
		conceptMap.setSource(new IdType(systemSource));
		conceptMap.setTarget(new IdType(systemTarget));

		// add concept mappings
		ConceptMap.ConceptMapGroupComponent group = conceptMap.addGroup();
		group.setSource(systemSource);
		group.setTarget(systemTarget);

		for (Entry<String, String> entry:sourceToTargetCodes.entrySet()) {
			group.addElement().setCode(entry.getKey()).addTarget().setCode(entry.getValue());
		}

		// set other relevant details
		conceptMap.setStatus(ACTIVE);
		conceptMap.setDate(new Date());

		conceptMap.setName(name);
		conceptMap.setDate(new Date());

		List<IBaseResource> resourceList = new ArrayList<>();
		resourceList.add(conceptMap);
		ArrayList<IBaseResource> resources = (ArrayList) client.transaction().withResources(resourceList).execute();
		String out = ((ConceptMap)resources.get(0)).getId();
		return out.split("/_history")[0];
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// OPERATION
	///////////////////////////////////////////////////////////////////////////////////////////////


	public ValidateCodeResultDTO validateMetadataResource(String theSystem, String theCode, MetadataResource mr) {
		ValidateCodeResultDTO result = null;

		if (mr.getUrl()!=null && !mr.getUrl().isEmpty()) {
			IBaseParameters params = ParametersUtil.newInstance(client.getFhirContext());

			ParametersUtil.addParameterToParametersUri(client.getFhirContext(), params, "url", mr.getUrl());//http://hapi.fhir.org/baseR4/ValueSet/PatientTaxSituation2
			ParametersUtil.addParameterToParametersString(client.getFhirContext(), params, "code", theCode);//G
			ParametersUtil.addParameterToParametersUri(client.getFhirContext(), params, "system", theSystem);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

			IBaseParameters output = null;

			if (mr instanceof ValueSet) {
				output = client
						.operation()
						.onType("ValueSet")
						.named("validate-code")
						.withParameters(params)
						.execute();
			} else {
				output = client
						.operation()
						.onType("CodeSystem")
						.named("validate-code")
						.withParameters(params)
						.execute();
			}


			Parameters out = (Parameters) output;

			Boolean value = ((BooleanType)out.getParameters("result").get(0)).booleanValue();
			String msg = (out.getParameters("message").get(0)).toString();

			result = new ValidateCodeResultDTO(value, msg);
		} else {
			IBaseParameters params = ParametersUtil.newInstance(client.getFhirContext());

			ParametersUtil.addParameterToParametersString(client.getFhirContext(), params, "code", theCode);//G
			ParametersUtil.addParameterToParametersUri(client.getFhirContext(), params, "system", theSystem);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

			IBaseParameters output = null;

			if (mr instanceof ValueSet) {
				output = client
						.operation()
						.onInstance("ValueSet/"+mr.getId())
						.named("validate-code")
						.withParameters(params)
						.execute();
			} else {
				output = client
						.operation()
						.onInstance("CodeSystem/"+mr.getId())
						.named("validate-code")
						.withParameters(params)
						.execute();
			}

			Parameters out = (Parameters) output;

			Boolean value = ((BooleanType)out.getParameters("result").get(0)).booleanValue();
			String msg = (out.getParameters("message").get(0)).toString();

			result = new ValidateCodeResultDTO(value, msg);

		}

		return result;
	}


	public CodeDTO translate(String system, String code, MetadataResource target) {

		IBaseParameters params = ParametersUtil.newInstance(client.getFhirContext());

		String targetID = target.getId().split("/_history")[0];

		ParametersUtil.addParameterToParametersUri(client.getFhirContext(), params, "target", targetID);//http://hapi.fhir.org/baseR4/ValueSet/PatientTaxSituation2
		ParametersUtil.addParameterToParametersString(client.getFhirContext(), params, "code", code);//G
		ParametersUtil.addParameterToParametersUri(client.getFhirContext(), params, "system", system);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

		IBaseParameters output = client
				.operation()
				.onType("ConceptMap")
				.named("translate")
				.withParameters(params)
				.execute();

		Parameters pars = (Parameters) output;
		CodeDTO out = null;
		for (ParametersParameterComponent ppc:pars.getParameter()) {
			if (ppc.getName().equalsIgnoreCase("match")) {
				Coding coding = (Coding) ppc.getPart().get(0).getValue();
				out = new CodeDTO(coding.getCode(), null, coding.getSystem());
				break;
			}
		}
		return out;
	}
*/

}