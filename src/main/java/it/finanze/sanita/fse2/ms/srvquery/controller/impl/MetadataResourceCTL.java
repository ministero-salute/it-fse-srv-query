package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IMetadataResourceCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.MetadataResourceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;

@RestController
public class MetadataResourceCTL extends AbstractCTL implements IMetadataResourceCTL {

	@Autowired
	private ITerminologySRV terminologySRV;
	
	@Override
	public MetadataResourceResponseDTO manageMetadataResource(List<SystemUrlDTO> requestBody) {
		List<MetadataResourceDTO> out = terminologySRV.manageMetadataResource(requestBody);
		return new MetadataResourceResponseDTO(getLogTraceInfo(), out);
	}

}
