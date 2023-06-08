/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.r4.model.CodeSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ILoadTestCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.LoadResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LoadTestResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Profile("dev")
public class LoadTestCTL extends AbstractCTL implements ILoadTestCTL {

	@Value("${terminology-server-url}")
    private String terminologyServerURL;

	@Value("${terminology-server-user}")
	private String terminologyServerUSR;
	
	@Value("${terminology-server-pwd}")
    private String terminologyServerPWD;
	
	@Override
	public LoadTestResponseDTO load(Integer startValue, Integer increaseStep, Integer numSteps) {
    	final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
    	List<LoadResultDTO> result = new ArrayList<>();

    	TerminologyClient tc = new TerminologyClient(terminologyServerURL, terminologyServerUSR, terminologyServerPWD);

    	List<CodeDTO> codes = new ArrayList<>();

        for (Integer n=0; n<numSteps; n++) {
        	LoadResultDTO dto = new LoadResultDTO();
        	try {
        		Long sampleSize = (long) startValue + (long) n*increaseStep;
            	dto.setSampleSize(sampleSize);
            	codes = loadCodes(codes, sampleSize);
            	
            	dto.setStartInsert(new Date());
            	String oid = UUID.randomUUID().toString();
				String id = tc.insertCS(oid, oid, null, codes);
            	dto.setStopInsert(new Date());
            	dto.setDurationInsertMS(getMS(dto.getStartInsert(), dto.getStopInsert()));

            	dto.setStartRead(new Date());
            	CodeSystem cs = tc.readCS(id);
            	dto.setStopRead(new Date());
            	dto.setDurationReadMS(getMS(dto.getStartRead(), dto.getStopRead()));

            	dto.setResult(cs.getConcept().size() == sampleSize);
        	} catch (Exception e) {
            	dto.setResult(false);
            	dto.setMsg(e.getMessage());

            	if (dto.getStartInsert()!=null && dto.getStopInsert()==null) {
                	dto.setStopInsert(new Date());
                	dto.setDurationInsertMS(getMS(dto.getStartInsert(), dto.getStopInsert()));
            	} else if (dto.getStartRead()!=null && dto.getStopRead()==null) {
                	dto.setStopRead(new Date());
                	dto.setDurationReadMS(getMS(dto.getStartRead(), dto.getStopRead()));
            	}

        	}
        	result.add(dto);
        	log.debug(new Gson().toJson(dto));
        	if (!dto.getResult()) {
        		break;
        	}
        }
		return new LoadTestResponseDTO(traceInfoDTO, result);
	}
    
	private List<CodeDTO> loadCodes(List<CodeDTO> codes, Long sampleSize) {
		List<CodeDTO> out = new ArrayList<>(codes);
		Long newCodes = sampleSize - out.size();
		for (Long i=0L;i<newCodes;i++) {
			String str = UUID.randomUUID().toString();
			CodeDTO code = new CodeDTO(str, str, str);
			out.add(code);
		}
		return out;
	}

	private long getMS(Date start, Date stop) {
		return stop.getTime() - start.getTime();
	}
	
}
