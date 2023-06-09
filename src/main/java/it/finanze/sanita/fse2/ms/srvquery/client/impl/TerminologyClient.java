package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import ca.uhn.fhir.rest.api.SummaryEnum;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.UriClientParam;
import ca.uhn.fhir.util.ParametersUtil;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.ValidateCodeResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.ResultPushEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TerminologyClient extends AbstractTerminologyClient {

	private IGenericClient tc;

	private String srvURL;
	
	public TerminologyClient(final String serverURL, final String username, final String pwd) {
		log.info("Terminology client initialize");
		tc = FHIRR4Helper.createClient(serverURL, username, pwd);
		srvURL = serverURL;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SVCM: Query Value Set [ITI-95]
	///////////////////////////////////////////////////////////////////////////////////////////////

	public ValueSet readVS(String id) {
		return read(tc, id, ValueSet.class);
	}

	public List<ValueSet> searchActiveValueSet() {
		return searchActive(tc, ValueSet.class);
	}

	public List<ValueSet> searchModifiedValueSet(Date start) {
		return searchModified(tc, start, ValueSet.class);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SVCM: Query Code System [ITI-96]
	///////////////////////////////////////////////////////////////////////////////////////////////

	public CodeSystem readCS(String id) {
		return read(tc, id, CodeSystem.class);
	}

	public List<CodeSystem> searchActiveCodeSystem() {
		return searchActive(tc, CodeSystem.class);
	}

	public List<CodeSystem> searchModifiedCodeSystem(Date start) {
		return searchModified(tc, start, CodeSystem.class);
	}
	
	public CodeSystem getCodeSystemVersionByIdAndDate(String id, Date date) {
        CodeSystem out = null;
        Bundle resultBundle = tc
            .search()
            .forResource(CodeSystem.class)
            .where(CodeSystem.RES_ID.exactly().code(id))
            .and(new DateClientParam("_lastUpdated").beforeOrEquals().millis(date))
            .sort().descending("_lastUpdated")
            .returnBundle(Bundle.class)
            .execute();

        List<Bundle.BundleEntryComponent> entries = resultBundle.getEntry();
        if (!entries.isEmpty()) {
            out = (CodeSystem) entries.get(0).getResource();
        }
        return out;
    }


	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SVCM: Expand Value Set [ITI-97]
	///////////////////////////////////////////////////////////////////////////////////////////////

	/*		
	String uuid = UUID.randomUUID().toString();
	String codeSystem = "{\"resourceType\":\"CodeSystem\",\"id\":\"example-codesystem" + uuid + "\",\"url\":\"http://localhost:8080/fhir/CodeSystem/example-codesystem" + uuid + "\",\"version\":\"1.0.0\",\"name\":\"Example CodeSystem\",\"status\":\"active\",\"content\":\"complete\",\"concept\":[{\"code\":\"gold\",\"display\":\"Gold\"},{\"code\":\"silver\",\"display\":\"Silver\"},{\"code\":\"bronze\",\"display\":\"Bronze\"}]}";
	tc.handlePullMetadataResource(codeSystem);
	String valueSetInclude = "{\"resourceType\":\"ValueSet\",\"id\":\"example-valueset" + uuid + "\",\"url\":\"http://localhost:8080/fhir/ValueSet/example-valueset" + uuid + "\",\"version\":\"1.0.0\",\"name\":\"Example ValueSet\",\"status\":\"active\",\"compose\":{\"include\":[{\"system\":\"http://localhost:8080/fhir/CodeSystem/example-codesystem" + uuid + "\"}]}}";
	tc.handlePullMetadataResource(valueSetInclude);

	Map<String, String> out = tc.expandVS("54104");
	for (Entry<String, String> c:out.entrySet()) {
		System.out.println(c.getKey() + " " + c.getValue());
	}
	 */

	public Map<String, String> expandVS(String id) {
		Map<String, String> out = new HashMap<>();

        Parameters response = tc
			.operation()
			.onInstance("ValueSet/"+id)
			.named("expand")
			.withNoParameters(Parameters.class)
			.execute();  
        
		ValueSet expandedValueSet = (ValueSet) response.getParameter().get(0).getResource();
		List<ValueSet.ValueSetExpansionContainsComponent> concepts = expandedValueSet.getExpansion().getContains();
		for (ValueSet.ValueSetExpansionContainsComponent concept : concepts) {
		    String code = concept.getCode();
		    String display = concept.getDisplay();
		    // Process each concept as needed
		    out.put(code, display);
		}		
		return out;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SVCM: Lookup Code [ITI-98]
	///////////////////////////////////////////////////////////////////////////////////////////////

//	tc.handlePullMetadataResource("{\"resourceType\":\"CodeSystem\",\"id\":\"loinc-codes\",\"url\":\"http://loinc.org\",\"version\":\"2.68\",\"name\":\"LOINC Codes\",\"title\":\"LOINC Codes\",\"status\":\"active\",\"content\":\"complete\",\"concept\":[{\"code\":\"LA6751-7\",\"display\":\"Glucose [Moles/volume] in Urine\"}]}");
//	tc.lookupMetadataResource("http://loinc.org", "LA6751-7");
	public Map<String, String> lookupMetadataResource(String url, String code) {
		
		IBaseParameters params = ParametersUtil.newInstance(tc.getFhirContext());
		ParametersUtil.addParameterToParametersString(tc.getFhirContext(), params, "code", code);
		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "system", url);


        // Perform the lookup operation
		IBaseParameters output = tc
                .operation()
                .onType(CodeSystem.class)
                .named("$lookup")
                .withParameters(params)
                .execute();
		Parameters out = (Parameters) output;
		
		Map<String, String> hashMap = new HashMap<>();
        for (ParametersParameterComponent ppc:out.getParameter()) {
            String name = ppc.getName();
            String value = ((IPrimitiveType<?>) ppc.getValue()).getValueAsString();

            hashMap.put(name, value);
        }
        
        System.out.println(hashMap);
		return hashMap;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SVCM: Validate Code [ITI-99]
	///////////////////////////////////////////////////////////////////////////////////////////////

	public ValidateCodeResultDTO validateMetadataResource(String theSystem, String theCode, MetadataResource mr) {
		ValidateCodeResultDTO result = null;

		IBaseParameters params = ParametersUtil.newInstance(tc.getFhirContext());
		ParametersUtil.addParameterToParametersString(tc.getFhirContext(), params, "code", theCode);//G
		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "system", theSystem);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"
		if (mr.getUrl()!=null && !mr.getUrl().isEmpty()) {

			ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "url", mr.getUrl());//http://hapi.fhir.org/baseR4/ValueSet/PatientTaxSituation2

			IBaseParameters output = null;

			if (mr instanceof ValueSet) {
				output = tc
						.operation()
						.onType("ValueSet")
						.named("validate-code")
						.withParameters(params)
						.execute();
			} else {
				output = tc
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
			IBaseParameters output = null;

			if (mr instanceof ValueSet) {
				output = tc
						.operation()
						.onInstance("ValueSet/"+mr.getId())
						.named("validate-code")
						.withParameters(params)
						.execute();
			} else {
				output = tc
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

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SVCM: Query Concept Map [ITI-100]
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public ConceptMap readCM(String id) {
		return read(tc, id, ConceptMap.class);
	}

	public List<ConceptMap> searchConceptMapBySourceSystem(MetadataResource mr) {
		List<ConceptMap> out = new ArrayList<>();

		if (mr!=null) {
			String sourceSystem = mr.getId().split("/_history")[0];

			Bundle bundle = tc.search()
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
	//									SVCM: Translate Code [ITI-101]
	///////////////////////////////////////////////////////////////////////////////////////////////

	public CodeDTO translate(String system, String code, MetadataResource target) {

		IBaseParameters params = ParametersUtil.newInstance(tc.getFhirContext());

		String targetID = target.getId().split("/_history")[0];

		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "target", targetID);//http://hapi.fhir.org/baseR4/ValueSet/PatientTaxSituation2
		ParametersUtil.addParameterToParametersString(tc.getFhirContext(), params, "code", code);//G
		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "system", system);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

		IBaseParameters output = tc
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
	
	public CodeDTO translate(String system, String code, MetadataResource source, MetadataResource target) {

		IBaseParameters params = ParametersUtil.newInstance(tc.getFhirContext());

		String targetID = target.getId().split("/_history")[0];
		String sourceID = source.getId().split("/_history")[0];

		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "source", sourceID);
		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "target", targetID);
		ParametersUtil.addParameterToParametersString(tc.getFhirContext(), params, "code", code);
		ParametersUtil.addParameterToParametersUri(tc.getFhirContext(), params, "system", system);

		IBaseParameters output = tc
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
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//											CUSTOM: INSERT
	///////////////////////////////////////////////////////////////////////////////////////////////

	public String insertCS(String oid, String name, String version, List<CodeDTO> codes) {
		return insertCS(tc, null, PublicationStatus.DRAFT, CodeSystemContentMode.FRAGMENT, oid, name, version, codes);
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

		return insertVS(tc, null, tmpUrl, PublicationStatus.ACTIVE, name, null, codesTxt);
	}

	public String insertCM(String name, final String url, MetadataResource mrSource, MetadataResource mrTarget, Map<String, String> sourceToTargetCodes) {

		ConceptMap conceptMap = new ConceptMap();

		String tmpUrl = url;
		if (tmpUrl==null || tmpUrl.isEmpty()) {
			tmpUrl = srvURL + "/ConceptMap/" + UUID.randomUUID().toString();
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
		conceptMap.setStatus(PublicationStatus.ACTIVE);
		conceptMap.setDate(new Date());
		
		conceptMap.setName(name);
		conceptMap.setDate(new Date());

		List<IBaseResource> resourceList = new ArrayList<>();
		resourceList.add(conceptMap);
		ArrayList<IBaseResource> resources = (ArrayList) tc.transaction().withResources(resourceList).execute();
		String out = ((ConceptMap)resources.get(0)).getId();
		return out.split("/_history")[0];
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//											CUSTOM: UPDATE
	///////////////////////////////////////////////////////////////////////////////////////////////

	public void updateCS(CodeSystem cs, List<CodeDTO> append) {
	    for (CodeDTO code : append) {
	        if (!isConceptAlreadyExists(cs, code.getCode())) {
	            ConceptDefinitionComponent cdc = new ConceptDefinitionComponent();
	            cdc.setDisplay(code.getDisplay());
	            cdc.setCode(code.getCode());
	            cs.getConcept().add(cdc);
	        }
	    }
	    tc.update().resource(cs).execute();
	}

	private boolean isConceptAlreadyExists(CodeSystem cs, String code) {
	    for (ConceptDefinitionComponent concept : cs.getConcept()) {
	        if (concept.getCode().equals(code)) {
	            return true; // Concept with the same code already exists
	        }
	    }
	    return false; // Concept with the same code doesn't exist
	}
	
	public CodeSystem getCodeSystemById(final String id) {
		return getCodeSystemByIdAndVersion(id, null);
	}
	
	public CodeSystem getCodeSystemByIdAndVersion(final String id, final String version) {
	    CodeSystem out = null;
	    IQuery<IBaseBundle> entry = tc
	            .search()
	            .forResource(CodeSystem.class)
	            .cacheControl(CacheControlDirective.noCache())
	            .where(CodeSystem.IDENTIFIER.exactly().identifier("urn:oid:"+id))
				.summaryMode(SummaryEnum.TRUE);

	    if (version != null) {
	        entry = entry.and(CodeSystem.VERSION.exactly().identifier(version));
	    }

	    Bundle results = entry.returnBundle(Bundle.class).execute();

	    // Process the search results
	    if (results != null && results.hasEntry()) {
	        // Access the code system resources
	        for (Bundle.BundleEntryComponent bundleEntry : results.getEntry()) {
	            if (bundleEntry.getResource() instanceof CodeSystem) {
	                out = (CodeSystem) bundleEntry.getResource();
	                break;
	            }
	        }
	    }

	    return out;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									CUSTOM: SUBSCRIPTION
	///////////////////////////////////////////////////////////////////////////////////////////////

	public void manageSubscription(SubscriptionEnum s, SubscriptionStatus subscriptionStatus, String url) {
		String criteria = s.getRisorsa() + "?status=" + s.getCriteria();
		Subscription existingSubscription = findSubscriptionForCriteria(tc, criteria);
		if (existingSubscription != null) {
			updateSubscription(tc, existingSubscription, subscriptionStatus, url);
		} else {
			Subscription subscription = buildSubscription(tc, s.getRisorsa(), s.getCriteria(), url, subscriptionStatus);
			createSubscription(tc, subscription);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									CUSTOM: DELETE METADATA RESOURCE
	///////////////////////////////////////////////////////////////////////////////////////////////

	public void deleteVS(String id) {
		delete(tc, id, ValueSet.class);
	}

	public void deleteCS(String id) {
		delete(tc, id, CodeSystem.class);
	}

	public void deleteCM(String id) {
		delete(tc, id, ConceptMap.class);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									CUSTOM: DICTIONARY PULL
	///////////////////////////////////////////////////////////////////////////////////////////////

	public ResultPushEnum handlePullMetadataResource(final String content) {
		ResultPushEnum out = null;
		try {
			MetadataResource mr = FHIRUtility.fromContentToMetadataResource(tc.getFhirContext(), content);
			mr.setStatus(PublicationStatus.DRAFT);
			if (!existMetadataResource(tc, mr)) {
				if (storeMetadataResource(tc, mr)) {
					out = ResultPushEnum.SAVED;
				}
			} else {
				out = ResultPushEnum.ALREADY_PRESENT; 
			}
		} catch(Exception ex) {
			out = ResultPushEnum.ERROR;
			log.error("Error while handle pull metadata resource:", ex);
		}
		
		if(out == null) {
			out = ResultPushEnum.ERROR;
		}
		
		return out;
	}
	
	
	public String transaction(CodeSystem codeSystem) {
		String location = "";
		try {
			Bundle transactionBundle = new Bundle();
			transactionBundle.setType(BundleType.TRANSACTION);

			// Aggiungi l'operazione per creare il CodeSystem al Bundle di transazione
			Bundle.BundleEntryComponent codeSystemEntry = new Bundle.BundleEntryComponent();
			codeSystemEntry.setResource(codeSystem);
			codeSystemEntry.getRequest().setMethod(HTTPVerb.POST);
			codeSystemEntry.getRequest().setUrl("CodeSystem");
			transactionBundle.addEntry(codeSystemEntry);

			Bundle response = tc.transaction().withBundle(transactionBundle).execute();
			// Process the search results
			if (response != null && response.hasEntry()) {
				location = response.getEntry().get(0).getResponse().getLocation();
			}
		} catch (Exception ex) {
			log.error("Error while performing transaction: ", ex);
			throw new BusinessException(ex.getMessage());
		}
		return location;
	}



}
