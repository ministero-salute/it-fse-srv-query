package it.finanze.sanita.fse2.ms.srvquery.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DelDocsResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.InvalidateExpansionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.ValueSetWarningDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;

@RequestMapping(path = "/v1/terminology")
@Tag(name = "Servizio ricerca terminologie FHIR")
@Validated
public interface ITerminologyCTL {
    
    @PostMapping(path = "/upload/{format}", produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "Upload avvenuta con successo", description = "Conversione in formato FHIR json.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Upload avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UploadResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    UploadResponseDTO uploadTerminology(
    		@PathVariable FormatEnum format,
			@RequestPart(required = false) RequestDTO creationInfo,
			@RequestPart(name = "file") MultipartFile file,HttpServletRequest request) throws IOException;
    
    @GetMapping(path = "/{oid}/{version}", produces = { MediaType.APPLICATION_JSON_VALUE })
 	@Operation(summary = "Upload avvenuta con successo", description = "Conversione in formato FHIR json.")
 	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
 	@ApiResponses(value = {
 			@ApiResponse(responseCode = "200", description = "Upload avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetResponseDTO.class))),
 			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
 			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    GetResponseDTO getTerminology(@PathVariable String oid,@PathVariable String version,HttpServletRequest request);
    
    @DeleteMapping(path = "/{idResource}", produces = { MediaType.APPLICATION_JSON_VALUE })
 	@Operation(summary = "Delete avvenuta con successo", description = "Delete avvenuta con successo.")
 	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
 	@ApiResponses(value = {
 			@ApiResponse(responseCode = "200", description = "Delete avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DelDocsResDTO.class))),
 			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
 			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    DelDocsResDTO deleteTerminology(@PathVariable String idResource, HttpServletRequest request);
		
    @PutMapping(path = "/{oidCS}/{versionCS}/invalidate-expansion", produces = { MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "Pre-espansioni invalidate con successo", description = "Le pre-espansioni di tutti i ValueSet derivati dal Code System indicato sono state invalidate.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Operazione avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InvalidateExpansionResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    InvalidateExpansionResponseDTO invalidateExpansion(@PathVariable String oidCS,@PathVariable String versionCS, HttpServletRequest request);

    @GetMapping(path = "/valueset-warning", produces = { MediaType.APPLICATION_JSON_VALUE })
 	@Operation(summary = "ValueSet warning", description = "ValueSet espansi prima della modifica dei CodeSystem che li generano.")
 	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
 	@ApiResponses(value = {
 			@ApiResponse(responseCode = "200", description = "Operazione avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValueSetWarningDTO.class))),
 			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
 			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ValueSetWarningDTO getValueSetWarning();
    
}
