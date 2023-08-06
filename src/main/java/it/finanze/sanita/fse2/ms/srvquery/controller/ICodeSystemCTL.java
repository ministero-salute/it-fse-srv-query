package it.finanze.sanita.fse2.ms.srvquery.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetActiveCSResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
 

@RequestMapping(path = "/v1/code-system")
@Tag(name = "Servizio creazione CodeSystem")
public interface ICodeSystemCTL {

    @PostMapping(value = "",  produces = {MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Creazione CodeSystem", description = "Creazione risorsa CodeSystem.")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateResponseDTO.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creazione risorsa avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateCodeSystemResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	ResponseEntity<CreateCodeSystemResDTO> insertCodeSystem(@RequestBody CreateCodeSystemReqDTO dto);

    
    /** 
     * Check if the document exists on the secondary storage (MongoDB). 
     * 
     * @param id  The ID of the document to search 
     * @param request  The HTTP Servlet Request 
     * @return ResourceExistResDTO  A DTO with a boolean, representing whether the resource already exists 
     */
    @GetMapping(value = "/get-active-resource")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetActiveCSResponseDTO.class)))
	@Operation(summary = "Controllo esistenza risorsa", description = "Controlla se su Elasticsearch è presente una risorsa con l'id fornito.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetActiveCSResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    GetActiveCSResponseDTO getActiveResource(HttpServletRequest request);


    /** 
     * Check if the document exists on the secondary storage (MongoDB). 
     * 
     * @param id  The ID of the document to search 
     * @param request  The HTTP Servlet Request 
     * @return ResourceExistResDTO  A DTO with a boolean, representing whether the resource already exists 
     */
    @GetMapping(value = "/get-active-resource/{id}/{format}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetResDTO.class)))
	@Operation(summary = "Controllo esistenza risorsa", description = "Controlla se su Elasticsearch è presente una risorsa con l'id fornito.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GetResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	GetResDTO getResource(@PathVariable String id, @PathVariable FormatEnum format, HttpServletRequest request);

}

