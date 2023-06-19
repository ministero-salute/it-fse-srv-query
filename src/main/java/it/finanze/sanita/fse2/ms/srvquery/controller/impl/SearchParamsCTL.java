/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ISearchParamsCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.SearchParameterResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class SearchParamsCTL extends AbstractCTL implements ISearchParamsCTL {

	
	@Autowired
	private IFHIRSRV fhirSRV;
	
	
	@Override
	public SearchParameterResponseDTO getAll() {
		return new SearchParameterResponseDTO(fhirSRV.getResourcesSearchParameters());
	}

}
