package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ITerminologyCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@RestController
public class TerminologyCTL extends AbstractCTL implements ITerminologyCTL {

	@Autowired
	private ITerminologySRV terminologySRV;


	@Override
	public UploadResponseDTO uploadTerminology(FormatEnum format, RequestDTO creationInfo, MultipartFile file, HttpServletRequest request) throws IOException {
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
		UploadResponseDTO out = terminologySRV.uploadTerminology(format,creationInfo,file);
		out.setSpanID(traceInfoDTO.getSpanID());
		out.setTraceID(traceInfoDTO.getTraceID());
		return out;
	}


	@Override
	public GetResponseDTO getTerminology(String oid, String version, HttpServletRequest request) {
		LogTraceInfoDTO trace = getLogTraceInfo();
		GetResponseDTO out = terminologySRV.isPresent(oid, version);
		out.setSpanID(trace.getSpanID());
		out.setTraceID(trace.getTraceID());
		return out;
	}


	@Override
	public void deleteTerminology(String idResource, HttpServletRequest request) {
		terminologySRV.deleteById(idResource);	
	}

}
