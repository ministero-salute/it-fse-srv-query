package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ICodeSystemCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@RestController
public class CodeSystemCTL extends AbstractCTL implements ICodeSystemCTL {

	@Autowired
	private ITerminologySRV terminologySRV;
	
	@Override
	public ResponseEntity<CreateCodeSystemResDTO> insertCodeSystem(CreateCodeSystemReqDTO dto) {
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
		String msg = terminologySRV.insertCodeSystem(dto.getName(), dto.getOid(), dto.getVersion(), dto.getCodes());		
		return new ResponseEntity<>(new CreateCodeSystemResDTO(traceInfoDTO, msg), HttpStatus.OK);
	}

}
