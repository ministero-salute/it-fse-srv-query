package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.gclient.StringClientParam;
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
public class TerminologyClient {

	private IGenericClient terminologyClient;


	public TerminologyClient(final String serverURL, final String username, final String pwd) {
		log.info("Terminology client initialize");
		terminologyClient = FHIRR4Helper.createClient(serverURL, username, pwd);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//											SUBSCRIPTION
	///////////////////////////////////////////////////////////////////////////////////////////////

	public void manageSubscription(SubscriptionEnum s, SubscriptionStatus subscriptionStatus, String url) {
		String criteria = s.getRisorsa() + "?status=" + s.getCriteria();
		Subscription existingSubscription = findExistingSubscription(criteria);
		if (existingSubscription != null) {
			updateSubscription(existingSubscription, subscriptionStatus, url);
		} else {
			Subscription subscription = buildSubscription(s.getRisorsa(), s.getCriteria(), url, subscriptionStatus);
			createSubscription(subscription);
		}
	}

	private void createSubscription(final Subscription subscription) {
		terminologyClient.create().resource(subscription).execute();
	}

	private Subscription findExistingSubscription(String criteria) {
		Bundle bundle = terminologyClient
				.search()
				.forResource(Subscription.class)
				.where(Subscription.CRITERIA.matches().value(criteria))
				.returnBundle(Bundle.class)
				.execute();

		for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
			if (entry.getResource() instanceof Subscription) {
				Subscription subscription = (Subscription) entry.getResource();
				return subscription;  
			}
		}

		return null; 
	}

	private void updateSubscription(Subscription subscription, SubscriptionStatus subscriptionStatus, String url) {
		subscription.setStatus(subscriptionStatus);
		subscription.getChannel().setEndpoint(url + "/" + subscription.getCriteria().split("=")[1]);
		terminologyClient.update().resource(subscription).execute();
	}

	private Subscription buildSubscription(String risorsa,String stato,String url,SubscriptionStatus subscriptionStatus) {
		Subscription subscription = new Subscription();
		subscription.setCriteria(risorsa+"?status="+ stato);
		subscription.setStatus(subscriptionStatus);
		subscription.setReason(risorsa+ " " + stato);
		Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent();
		channel.setType(Subscription.SubscriptionChannelType.RESTHOOK);
		channel.setEndpoint(url+ "/" + stato);
		channel.setPayload("application/json");
		subscription.setChannel(channel);
		return subscription;
	}


	///////////////////////////////////////////////////////////////////////////////////////////////
	//									READ METADATA RESOURCE
	///////////////////////////////////////////////////////////////////////////////////////////////

	private <T> T read(String id, Class<? extends MetadataResource> mr) {
		try {
			return (T) terminologyClient.read().resource(mr).withId(id).execute();
		} catch(Exception ex) {
			log.error("Errore while perform read client method:", ex);
			throw new BusinessException("Errore while perform read client method:", ex);
		}
	}

	public ValueSet readVS(String id) {
		return read(id, ValueSet.class);
	}

	public CodeSystem readCS(String id) {
		return read(id, CodeSystem.class);
	}

	public ConceptMap readCM(String id) {
		return read(id, ConceptMap.class);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									DELETE METADATA RESOURCE
	///////////////////////////////////////////////////////////////////////////////////////////////

	private <T> void delete(String id, Class<? extends MetadataResource> mr) {
		T res = (T) read(id, mr);
		terminologyClient.delete().resource((MetadataResource)res).execute();
	}

	public void deleteVS(String id) {
		delete(id, ValueSet.class);
	}

	public void deleteCS(String id) {
		delete(id, CodeSystem.class);
	}

	public void deleteCM(String id) {
		delete(id, ConceptMap.class);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	//									SEARCH METADATA RESOURCE
	///////////////////////////////////////////////////////////////////////////////////////////////

	private <T> List<T> searchActive(Class<? extends MetadataResource> mr) {
		List<T> out = new ArrayList<>();
		try {
			Bundle bundle = terminologyClient.search().forResource(mr).cacheControl(CacheControlDirective.noCache())
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
			Bundle bundle = terminologyClient.search().forResource(mr).cacheControl(CacheControlDirective.noCache())
					.where(CodeSystem.DATE.afterOrEquals().millis(start)).returnBundle(Bundle.class).execute();
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

			Bundle bundle = terminologyClient.search()
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
	//											INSERT
	///////////////////////////////////////////////////////////////////////////////////////////////

	public String insertCS(String oid, String name, String version, List<CodeDTO> codes) {
		return insertCS(null, PublicationStatus.DRAFT, CodeSystemContentMode.COMPLETE, oid, name, version, codes);
	}

	private String insertCS(final String id, final PublicationStatus ps, final CodeSystemContentMode cscm, String oid, String name, String version, List<CodeDTO> codes) {
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

		if (oid!=null && !oid.isEmpty()) {
			List<Identifier> ids = new ArrayList<>();
			Identifier identifier = new Identifier();
			identifier.setSystem("urn:ietf:rfc:3986");
			identifier.setValue("urn:oid:" + oid);
			ids.add(identifier);
			codeSystem.setIdentifier(ids);
		}

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

		Bundle response = terminologyClient.transaction().withBundle(transactionBundle).execute();
		Boolean flagStatus = response.getEntryFirstRep().getResponse().getStatus().equalsIgnoreCase("201 Created");
		String out = null;
		if (flagStatus!=null && flagStatus) {			
			out = response.getEntryFirstRep().getResponse().getLocation().split("/")[1];
		}
		return out;
	}

	/*
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

		return insertVS(null, tmpUrl, PublicationStatus.ACTIVE, name, null, codesTxt);
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
		ArrayList<IBaseResource> resources = (ArrayList) terminologyClient.transaction().withResources(resourceList).execute();
		String out = ((ValueSet)resources.get(0)).getId();
		return out.split("/_history")[0];
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
		ArrayList<IBaseResource> resources = (ArrayList) terminologyClient.transaction().withResources(resourceList).execute();
		String out = ((ConceptMap)resources.get(0)).getId();
		return out.split("/_history")[0];
	}
	 */

	///////////////////////////////////////////////////////////////////////////////////////////////
	//											OPERATION
	///////////////////////////////////////////////////////////////////////////////////////////////


	public ValidateCodeResultDTO validateMetadataResource(String theSystem, String theCode, MetadataResource mr) {
		ValidateCodeResultDTO result = null;

		if (mr.getUrl()!=null && !mr.getUrl().isEmpty()) {
			IBaseParameters params = ParametersUtil.newInstance(terminologyClient.getFhirContext());

			ParametersUtil.addParameterToParametersUri(terminologyClient.getFhirContext(), params, "url", mr.getUrl());//http://hapi.fhir.org/baseR4/ValueSet/PatientTaxSituation2
			ParametersUtil.addParameterToParametersString(terminologyClient.getFhirContext(), params, "code", theCode);//G
			ParametersUtil.addParameterToParametersUri(terminologyClient.getFhirContext(), params, "system", theSystem);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

			IBaseParameters output = null;

			if (mr instanceof ValueSet) {
				output = terminologyClient
						.operation()
						.onType("ValueSet")
						.named("validate-code")
						.withParameters(params)
						.execute();
			} else {
				output = terminologyClient
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
			IBaseParameters params = ParametersUtil.newInstance(terminologyClient.getFhirContext());

			ParametersUtil.addParameterToParametersString(terminologyClient.getFhirContext(), params, "code", theCode);//G
			ParametersUtil.addParameterToParametersUri(terminologyClient.getFhirContext(), params, "system", theSystem);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

			IBaseParameters output = null;

			if (mr instanceof ValueSet) {
				output = terminologyClient
						.operation()
						.onInstance("ValueSet/"+mr.getId())
						.named("validate-code")
						.withParameters(params)
						.execute();
			} else {
				output = terminologyClient
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

		IBaseParameters params = ParametersUtil.newInstance(terminologyClient.getFhirContext());

		String targetID = target.getId().split("/_history")[0];

		ParametersUtil.addParameterToParametersUri(terminologyClient.getFhirContext(), params, "target", targetID);//http://hapi.fhir.org/baseR4/ValueSet/PatientTaxSituation2
		ParametersUtil.addParameterToParametersString(terminologyClient.getFhirContext(), params, "code", code);//G
		ParametersUtil.addParameterToParametersUri(terminologyClient.getFhirContext(), params, "system", system);//"http://hapi.fhir.org/baseR4/CodeSystem/PatientTaxSituation"

		IBaseParameters output = terminologyClient
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
	//											LOGGING
	///////////////////////////////////////////////////////////////////////////////////////////////

	class LoggingInterceptor implements IClientInterceptor {

		private final ThreadLocal<UUID> uniqueIdThreadLocal = new ThreadLocal<>();

		@Override
		public void interceptRequest(IHttpRequest theRequest) {
			String req = theRequest.toString();
			String body = "";
			try {
				body = theRequest.getRequestBodyFromStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			UUID uniqueId = UUID.randomUUID();
			uniqueIdThreadLocal.set(uniqueId);
			log.debug("\n==== REQUEST ===");
			log.debug("Unique ID: " + uniqueId);
			log.debug(req);
			if (body!=null) {
				log.debug(body);
			}
			log.debug("================\n");
		}

		@Override
		public void interceptResponse(IHttpResponse theResponse) throws IOException {
			UUID uniqueId = uniqueIdThreadLocal.get();
			uniqueIdThreadLocal.remove();
			log.debug("\n==== RESPONSE ===");
			log.debug("Unique ID: " + uniqueId);
			log.debug("" + theResponse.getStatus());
			log.debug("================\n");
		}
	}


	public ResultPushEnum handlePullMetadataResource(final String content) {
		ResultPushEnum out = null;
		try {
			MetadataResource mr = FHIRUtility.fromContentToMetadataResource(terminologyClient.getFhirContext(), content);
			if (!existMetadataResource(mr)) {
				if (storeMetadataResource(mr)) {
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

	private boolean storeMetadataResource(MetadataResource mr) {
		return terminologyClient.create().resource(mr).execute().getId().getValue()!=null;
	}

	private boolean existMetadataResource(MetadataResource mr) {

		Bundle response = terminologyClient.search().forResource(mr.getClass())
				.where(new StringClientParam("url").matches().value(mr.getUrl()))
				.and(new StringClientParam("version").matches().value(mr.getVersion())).returnBundle(Bundle.class)
				.execute();            
		return response.getEntry()!=null && !response.getEntry().isEmpty();
	}

}
