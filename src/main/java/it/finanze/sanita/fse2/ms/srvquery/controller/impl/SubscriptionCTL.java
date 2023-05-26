package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.ISubscriptionCTL;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@RestController
public class SubscriptionCTL implements ISubscriptionCTL {

	@Autowired
	private ITerminologySRV terminologySRV;
	
	@Override
	public void manageSubscription(SubscriptionEnum target, SubscriptionStatus status) {
		terminologySRV.manageSubscription(target, status);		
	}

}
