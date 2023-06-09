package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTerminologyClient {

	protected <T> T read(IGenericClient tc, String id, Class<? extends MetadataResource> mr) {
		try {
			return (T) tc.read().resource(mr).withId(id).execute();
		} catch(Exception ex) {
			log.error("Errore while perform read client method:", ex);
			throw new BusinessException("Errore while perform read client method:", ex);
		}
	}

	protected <T> List<T> searchActive(IGenericClient tc, Class<? extends MetadataResource> mr) {
		List<T> out = new ArrayList<>();
		try {
			Bundle bundle = tc.search().forResource(mr).cacheControl(CacheControlDirective.noCache())
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

	protected <T> List<T> searchModified(IGenericClient tc, Date start, Class<? extends MetadataResource> mr) {
		List<T> out = new ArrayList<>();
		try {
			Bundle bundle = tc.search().forResource(mr).cacheControl(CacheControlDirective.noCache())
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
	
	protected void createSubscription(IGenericClient tc, final Subscription subscription) {
		tc.create().resource(subscription).execute();
	}

	protected Subscription findSubscriptionForCriteria(IGenericClient tc, String criteria) {
		Bundle bundle = tc
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

	protected void updateSubscription(IGenericClient tc, Subscription subscription, SubscriptionStatus subscriptionStatus, String url) {
		subscription.setStatus(subscriptionStatus);
		subscription.getChannel().setEndpoint(url + "/" + subscription.getCriteria().split("=")[1]);
		tc.update().resource(subscription).execute();
	}

	protected Subscription buildSubscription(IGenericClient tc, String risorsa,String stato,String url,SubscriptionStatus subscriptionStatus) {
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
	
	protected <T> void delete(IGenericClient tc, String id, Class<? extends MetadataResource> mr) {
		T res = (T) read(tc, id, mr);
		tc.delete().resource((MetadataResource)res).execute();
	}

	protected boolean storeMetadataResource(IGenericClient tc, MetadataResource mr) {
		IIdType id = tc.create().resource(mr).execute().getId();
		return id.getValue()!=null;
	}

	protected boolean existMetadataResource(IGenericClient tc, MetadataResource mr) {
		Bundle response = tc.search().forResource(mr.getClass())
				.where(new StringClientParam("url").matches().value(mr.getUrl()))
				.and(new StringClientParam("version").matches().value(mr.getVersion())).returnBundle(Bundle.class)
				.execute();            
		return response.getEntry()!=null && !response.getEntry().isEmpty();
	}

	protected String insertCS(IGenericClient tc, final String id, final PublicationStatus ps, final CodeSystemContentMode cscm, String oid, String name, String version, List<CodeDTO> codes) {
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
		codeSystem.setCopyright("Copyright");

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

		Bundle response = tc.transaction().withBundle(transactionBundle).execute();
		Boolean flagStatus = response.getEntryFirstRep().getResponse().getStatus().equalsIgnoreCase("201 Created");
		String out = null;
		if (flagStatus!=null && flagStatus) {			
			out = response.getEntryFirstRep().getResponse().getLocation().split("/")[1];
		}
		return out;
	}
	
	protected String insertVS(final IGenericClient tc, final String id, final String url, final PublicationStatus ps, String name, String version, Map<String, List<CodeDTO>> codes) {
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
		ArrayList<IBaseResource> resources = (ArrayList) tc.transaction().withResources(resourceList).execute();
		String out = ((ValueSet)resources.get(0)).getId();
		return out.split("/_history")[0];
	}
	
}
