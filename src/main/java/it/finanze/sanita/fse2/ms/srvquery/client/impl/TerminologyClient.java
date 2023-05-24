package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TerminologyClient {

	private IGenericClient terminologyClient;


	public TerminologyClient(final String serverURL, final String username, final String pwd) {
		log.info("Terminology client initialize");
		terminologyClient = FHIRR4Helper.createClient(serverURL, username, pwd);
	}

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

}
