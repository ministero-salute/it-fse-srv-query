/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.ITerminologyServiceCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CodeTranslationResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.service.IFHIRSRV;


/** 
 * @author Riccardo Bonesi
 */
@RestController
public class TerminologyServiceCTL extends AbstractCTL implements ITerminologyServiceCTL {

	/** 
	 * FHIR Service 
	 */
    @Autowired
    private IFHIRSRV FHIRSRV;
    
    
    @Override
    public ResponseDTO getCodeSystems(final HttpServletRequest request) {

        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO getCodeSystem(final String id, final HttpServletRequest request) {

        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO codeSystemLookUp(final String system, final String code, final HttpServletRequest request) {
        
        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO codeSystemValidateCode(final String code, final String display, final HttpServletRequest request) {

        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO codeSystemSubsumes(final String system, final String codeA, final String codeB, final HttpServletRequest request) {
        
        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO getValueSets(final HttpServletRequest request) {
        
        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO getValueSet(final String id, final HttpServletRequest request) {
        
        // TODO

        return new ResponseDTO();
    }

    @Override
    public ResponseDTO valueSetValidateCode(final String system, final String code, final HttpServletRequest request) {
        
        // TODO

        return new ResponseDTO();
    }

    @Override
    public CodeTranslationResDTO valueSetTranslate(final String code, final String system, final String targetSystem, final HttpServletRequest request) {
        
        String result = FHIRSRV.translateCode(code, system, targetSystem);

        return new CodeTranslationResDTO(getLogTraceInfo(), result);
    }


    
}
