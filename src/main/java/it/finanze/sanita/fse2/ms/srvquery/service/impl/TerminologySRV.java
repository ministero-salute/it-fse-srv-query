package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.IWebScrapingClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.config.TerminologyCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.ResultPushEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
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
	
	@Autowired
	private IWebScrapingClient client;
	
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
	
	
	@Override
	public CreateCodeSystemResDTO manageCodeSystem(final CreateCodeSystemReqDTO dto) {
		CreateCodeSystemResDTO out = new CreateCodeSystemResDTO();
		
		CodeSystem codeSystem = getCodeSystemById("urn:oid:"+dto.getOid());
		
		if(codeSystem==null) {
			String id = insertCodeSystem(dto.getName(), dto.getOid(), dto.getVersion(), dto.getCodes()); 
			out.setId(id);
		} else if(PublicationStatus.DRAFT.equals(codeSystem.getStatusElement().getValue())) {
			 updateCodeSystem(codeSystem, dto.getCodes());
			 out.setId(codeSystem.getId());
		}
		
		return out;
	}
	
	private CodeSystem getCodeSystemById(final String oid) {
		TerminologyClient terminologyClient = getTerminologyClient();
		return terminologyClient.getCodeSystemById(oid);
	}
	
	private void updateCodeSystem(final CodeSystem codeSystem, List<CodeDTO> codes) {
		TerminologyClient terminologyClient = getTerminologyClient();
		terminologyClient.updateCS(codeSystem, codes);
	}
	
	@Override
	public List<MetadataResourceDTO> manageMetadataResource(final List<SystemUrlDTO> list) {
		TerminologyClient terminologyClient = getTerminologyClient();
		List<MetadataResourceDTO> out = new ArrayList<>();		
		
		for(SystemUrlDTO entry : list) {
			ResultPushEnum esito = null;
			String res = client.webScraper(entry.getUrl());
			if(StringUtility.isNullOrEmpty(res)) {
				esito = ResultPushEnum.RESOURCE_NOT_FOUND;
				out.add(new MetadataResourceDTO(entry.getSystem(),entry.getUrl(),  esito));
				continue;
			}
			 esito = terminologyClient.handlePullMetadataResource(res);
			 out.add(new MetadataResourceDTO(entry.getSystem(), entry.getUrl(),esito));
		}
		return out;
	}
	
}
