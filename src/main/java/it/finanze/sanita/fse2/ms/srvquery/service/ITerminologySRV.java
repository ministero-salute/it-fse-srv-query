package it.finanze.sanita.fse2.ms.srvquery.service;

import java.util.List;

import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;

import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;

public interface ITerminologySRV {

	void manageSubscription(SubscriptionEnum subscriptionEnum, SubscriptionStatus actionEnum);

	String insertCodeSystem(String name, String oid, String version, List<CodeDTO> codes);
}
