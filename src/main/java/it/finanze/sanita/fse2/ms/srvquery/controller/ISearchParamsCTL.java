package it.finanze.sanita.fse2.ms.srvquery.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.SearchParameterResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;

@RequestMapping(path = "/v1/searchParams")
@Tag(name = "Servizio gestione parametri di ricerca risorse FHIR")
@Validated
public interface ISearchParamsCTL {

	@GetMapping(value = "")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SearchParameterResponseDTO.class)))
	@Operation(summary = "Controllo esistenza risorsa", description = "Controlla se su Elasticsearch è presente una risorsa con l'id fornito.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SearchParameterResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	SearchParameterResponseDTO getAll();



}
