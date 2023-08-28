package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ITerminologyCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.InvalidateResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DelDocsResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.InvalidateExpansionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.ValueSetWarningDTO;
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
	public DelDocsResDTO deleteTerminology(String idResource, HttpServletRequest request) {
		terminologySRV.deleteById(idResource);
		return new DelDocsResDTO(getLogTraceInfo());
	}


	@Override
	public InvalidateExpansionResponseDTO invalidateExpansion(String oidCS, String versionCS, HttpServletRequest request) {
		List<InvalidateResultDTO> invInfo = terminologySRV.invalidateExpansion(oidCS, versionCS);
		return new InvalidateExpansionResponseDTO(getLogTraceInfo(), invInfo);
	}


	@Override
	public ValueSetWarningDTO getValueSetWarning() {
		List<ValueSet> valuesets = terminologySRV.getValueSetWarning();
		return new ValueSetWarningDTO(getLogTraceInfo(), valuesets);
	}

}
