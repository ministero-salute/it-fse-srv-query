package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.FHIRClient;
import it.finanze.sanita.fse2.ms.srvquery.config.FHIRCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;


@Service
@Slf4j
public class FHIRSRV implements IFHIRSRV {

    @Autowired
	private ProfileUtility profileUtility;

    @Autowired
	private FHIRCFG FHIRCFG;

    private FHIRClient client;

    @PostConstruct
    void init() {
        this.client = new FHIRClient(FHIRCFG.getFhirServerTestUrl());
    }

    @Override
	public Boolean create(final FhirPublicationDTO createDTO) {
		boolean out = false;
		try {
            if (profileUtility.isDevProfile()) {
                String json = createDTO.getJsonString();
                Bundle bundle = FHIRR4Helper.deserializeResource(Bundle.class, json, true);

                client.saveBundleWithTransaction(bundle);
                out = true;
                log.info("FHIR bundle: {}", json);
            } else {
                // TODO
            }

		} catch(Exception e) {
			log.error("Error creating new resource on FHIR Server: ", e);
			throw new BusinessException(e);
		}
		return out;
	}

    @Override
    public String translateCode(String code, String system, String targetSystem) {
        String out = "";
		try {
            if (profileUtility.isDevProfile()) {
                FHIRClient client = new FHIRClient(FHIRCFG.getFhirServerTestUrl());
                out = client.translateCode(code, system, targetSystem);
                log.info("Code translated result: {}", out);
            } else {
                // TODO
            }

		} catch(Exception e) {
			log.error("Error translating Code from FHIR Terminology Server: ", e);
			throw new BusinessException(e);
		}
		return out;
    }

    @Override
    public boolean checkExist(String masterIdentifier) {
        boolean isFound = this.client.read(masterIdentifier);
        log.info("found?: {}", isFound);
        return isFound;
    }
}
