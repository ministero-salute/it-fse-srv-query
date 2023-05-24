package it.finanze.sanita.fse2.ms.srvquery.service;

import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;

import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;

public interface ITerminologySRV {

	void manageSubscription(SubscriptionEnum subscriptionEnum, SubscriptionStatus actionEnum);
}
