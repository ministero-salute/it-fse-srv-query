package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import java.util.List;

import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.config.TerminologyCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Service Implementation 
 */
@Service
@Slf4j
public class TerminologySRV implements ITerminologySRV {

	@Autowired
	private TerminologyCFG terminologyCFG;

	private TerminologyClient terminologyClient;
	
    private TerminologyClient getTerminologyClient() {
        if (terminologyClient == null) {
            synchronized (this) {
                if (terminologyClient == null) {
                    terminologyClient = new TerminologyClient(terminologyCFG.getFhirServerUrl(), terminologyCFG.getFhirServerUser(), terminologyCFG.getFhirServerPwd());
                }
            }
        }
        return terminologyClient;
    }

	@Override
	public void manageSubscription(SubscriptionEnum subscriptionEnum, SubscriptionStatus actionEnum) {
        TerminologyClient terminologyClient = getTerminologyClient();
		
		if(SubscriptionEnum.ALL.equals(subscriptionEnum)) {
			for(SubscriptionEnum s : SubscriptionEnum.values()) {
				if(SubscriptionEnum.ALL.equals(s)) {
					continue;
				}
				terminologyClient.manageSubscription(s,actionEnum,terminologyCFG.getPolicyManagerUrl());		
			}
		} else {
			terminologyClient.manageSubscription(subscriptionEnum,actionEnum,terminologyCFG.getPolicyManagerUrl());
		}
	}

	@Override
	public String insertCodeSystem(String name, String oid, String version, List<CodeDTO> codes) {
        TerminologyClient terminologyClient = getTerminologyClient();
        return terminologyClient.insertCS(oid, name, version, codes);
	}
	
}
