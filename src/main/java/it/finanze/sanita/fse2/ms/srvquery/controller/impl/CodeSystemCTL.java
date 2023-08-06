package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ICodeSystemCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.GetActiveResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetActiveCSResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@RestController
public class CodeSystemCTL extends AbstractCTL implements ICodeSystemCTL {

	@Autowired
	private ITerminologySRV terminologySRV;
	
	@Override
	public ResponseEntity<CreateCodeSystemResDTO> insertCodeSystem(CreateCodeSystemReqDTO dto) {
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
		CreateCodeSystemResDTO out = terminologySRV.manageCodeSystem(dto);
		out.setSpanID(traceInfoDTO.getSpanID());
		out.setTraceID(traceInfoDTO.getTraceID());
		return new ResponseEntity<>(out, HttpStatus.OK);
	}

	@Override
	public GetActiveCSResponseDTO getActiveResource(HttpServletRequest request) {
		List<GetActiveResourceDTO> list = terminologySRV.getSummaryNameActiveResource();
		return new GetActiveCSResponseDTO(getLogTraceInfo(), list);
	}
	 
	@Override
	public GetResDTO getResource(String id,FormatEnum format, HttpServletRequest request) {
		return terminologySRV.export(id, format);
	}

}
