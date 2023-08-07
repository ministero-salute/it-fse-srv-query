package it.finanze.sanita.fse2.ms.srvquery.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.SearchResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.MetadataResourceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.TranslateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.LanguageEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.TypeEnum;

@RequestMapping(path = "/v1/metadata-resource")
@Tag(name = "Servizio gestione parametri di ricerca risorse FHIR")
@Validated
public interface IMetadataResourceCTL {

	@PostMapping(value = "",  produces = {MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Gestione metadata resource", description = "Gestione metadata resource")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MetadataResourceResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	@ResponseStatus(HttpStatus.OK)
	MetadataResourceResponseDTO manageMetadataResource(@RequestBody List<SystemUrlDTO> requestBody);

	@PostMapping(value = "/translate",  produces = {MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Traduci metadata resource", description = "Traduci metadata resource")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TranslateResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	@ResponseStatus(HttpStatus.OK)
	TranslateResponseDTO translateCodeSystem(String id, LanguageEnum from, LanguageEnum to);

	@GetMapping(value = "/{identifier}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class)))
	@Operation(summary = "Recupera la diff tra due metadata resource", description = "Recupera la diff tra due metadata resource.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Record not found", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	SearchResultDTO searchResourceByIdAndVersion(
			@PathVariable String identifier,
			@RequestParam (required = false) String versionFrom,
			@RequestParam (required = false) String versionTo,
			@RequestParam TypeEnum type, HttpServletRequest request);
}
