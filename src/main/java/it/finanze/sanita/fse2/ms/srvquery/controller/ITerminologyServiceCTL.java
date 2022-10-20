/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CodeTranslationResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;



/**
 * 
 * @author Riccardo Bonesi
 *
 * Controller used to manage FHIR Terminology Service resources.
 */
@RequestMapping(path = "/v1/terminology-service")
@Tag(name = "Servizio per recupero terminologie FHIR dal Terminology Service")
@Validated
public interface ITerminologyServiceCTL {

	/** 
	 * Returns all Code Systems from FHIR Server. 
	 * 
	 * @param request  The HTTP Servlet Request 
	 * @return ResponseDTO  A DTO representing the response 
	 */
    @GetMapping(value = "/codeSystems")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "Recupero codeSystems", description = "Recupera tutti i codeSystem.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO getCodeSystems(HttpServletRequest request);

    /** 
     * Returns a Code System given its ID. 
     * 
     * @param id  The code system ID 
     * @param request  The HTTP Servlet Request 
	 * @return ResponseDTO  A DTO representing the response 
     */
    @GetMapping(value = "/codeSystem/{id}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "Recupero codeSystem", description = "Recupera l'istanza di un codeSystem dato il suo id.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO getCodeSystem(@PathVariable(required = true, name = "id") String id, HttpServletRequest request);

    /** 
     * Returns additional info regarding the Code System 
     * 
     * @param system  System
     * @param code  Code 
     * @param request  The HTTP Servlet Request 
	 * @return ResponseDTO  A DTO representing the response 
     */
    @GetMapping(value = "/codeSystem/lookup")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "CodeSystem lookup", description = "Ritorna informazioni aggiuntive.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO codeSystemLookUp(@RequestParam("system") String system, @RequestParam("code") String code, HttpServletRequest request);

    /** 
     * Verifies the presence of the Code in the Code System 
     * 
     * @param code  The code to be checked 
     * @param display  Display 
     * @param request  The HTTP Servlet Request 
	 * @return ResponseDTO  A DTO representing the response 
     */
    @GetMapping(value = "/codeSystem/validate-code")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "CodeSystem validate code", description = "Verifica presenza Code in CodeSystem.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO codeSystemValidateCode(@RequestParam("code") String code, @RequestParam("display") String display, HttpServletRequest request);

    /** 
     * Verifies relationship between codes. 
     * 
     * @param system  The system 
     * @param codeA  The first code 
     * @param codeB  The second code
     * @param request  The HTTP Servlet Request
     * @return ResponseDTO  A DTO representing the response
     */
    @GetMapping(value = "/codeSystem/subsumes")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "CodeSystem subsumes", description = "Verifica relazioni tra codici.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO codeSystemSubsumes(@RequestParam("system") String system, @RequestParam("codeA") String codeA, @RequestParam("codeB") String codeB, HttpServletRequest request);

    /** 
     * Retrieves all the Value Sets from the FHIR Server
     * 
     * @param request  The HTTP Servlet Request 
     * @return ResponseDTO  A DTO representing the response
     */
	@GetMapping(value = "/valueSets")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "Recupero ValueSets", description = "Recupera tutti i ValueSets.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO getValueSets(HttpServletRequest request);

	/** 
	 * Retrieves a Value Set given its ID 
	 * 
	 * @param id  The ID of the Value Set 
	 * @param request  The HTTP Servlet Request 
	 * @return ResponseDTO  A DTO representing the response 
	 */
    @GetMapping(value = "/valueSet/{id}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "Recupero ValueSet", description = "Fornisce il contenuto del ValueSet.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO getValueSet(@PathVariable(required = true, name = "id") String id, HttpServletRequest request);

    /** 
     * Verifies the presence of the code into a Value Set 
     * 
     * @param system  The coe system 
     * @param code  The code
     * @param request The HTTP Servlet Request 
     * @return ResponseDTO  A DTO representing the response 
     */
    @GetMapping(value = "/valueSet/validate-code")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class)))
	@Operation(summary = "Verifica presenza Code in Valueset.", description = "Verifica la presenza di un Code nel Valueset.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResponseDTO valueSetValidateCode(@RequestParam("system") String system, @RequestParam("code") String code, HttpServletRequest request);


    /** 
     * Translate a code from a Value Set to another one. 
     * 
     * @param code  The code
     * @param system  The starting system 
     * @param targetSystem  The target system 
     * @param request  The HTTP Servlet Request 
     * @return CodeTranslationResDTO  A DTO with the result of the translation 
     */
    @GetMapping(value = "/conceptMap/translate")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CodeTranslationResDTO.class)))
	@Operation(summary = "Traduzione Code", description = "Traduce un Code da un ValueSet ad un altro.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CodeTranslationResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	CodeTranslationResDTO valueSetTranslate(@RequestParam("code") String code, @RequestParam("system") String system, @RequestParam("targetSystem") String targetSystem, HttpServletRequest request);

}
