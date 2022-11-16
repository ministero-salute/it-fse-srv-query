/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.controller;

import static it.finanze.sanita.fse2.ms.srvquery.utility.ValidationUtility.DEFAULT_STRING_MAX_SIZE;
import static it.finanze.sanita.fse2.ms.srvquery.utility.ValidationUtility.DEFAULT_STRING_MIN_SIZE;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.DeleteResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ReplaceResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResourceExistResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.UpdateResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceAlreadyPresentException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;

/**
 * 
 *
 * Controller used to manage FHIR resources.
 */
@RequestMapping(path = "/v1/document")
@Tag(name = "Servizio gestione risorse FHIR")
@Validated
public interface IDocumentCTL {

	/** 
	 * Creates a new FHIR Resource on FHIR Server. After creation, sends a message 
	 * on a Kafka topic, for creation of the Document on MongoDB. 
	 * 
	 * @param request  The HTTP Servlet Request 
	 * @param body  The body of the request 
	 * @return ResponseDTO  A DTO representing the response 
	 * @throws IOException  Generic IO Exception 
	 * @throws OperationException  Generic MongoDB Exception 
	 * @throws ResourceAlreadyPresentException  An exception thrown when the resource is already present on FHIR Server 
	 */
    @PostMapping(value = "/create",  produces = {MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Create a new bundle in a server fhir", description = "Servizio che consente di creare un nuovo bundle all'interno del Server FHIR.")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = CreateResponseDTO.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creazione risorsa avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    CreateResponseDTO create(@RequestBody FhirPublicationDTO body,HttpServletRequest request);

    /** 
     * Deletes a document on FHIR Server. 
     * 
     * @param request  The HTTP Servlet Request 
     * @param identifier  The identifier of the document to be deleted 
	 * @return ResponseDTO  A DTO representing the response 
     * @throws ResourceNotFoundException  An exception thrown when the resource is not found on MongoDB 
     * @throws OperationException  Generic MongoDB Exception 
     */
    @DeleteMapping(value = "/delete/{identifier}",  produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Delete a FHIR resource", description = "Servizio che consente di cancellare una risorsa FHIR dal Server FHIR.")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = DeleteResponseDTO.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellazione risorsa avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DeleteResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Risorsa non trovata", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    DeleteResponseDTO delete(@PathVariable @Size(min = DEFAULT_STRING_MIN_SIZE, max = DEFAULT_STRING_MAX_SIZE, message = "resourceId does not match the expected size") String identifier,HttpServletRequest request);

    /** 
     * Replaces an existing document on FHIR Server (it performs a deletion plus a new creation) 
     *
     * @param request  The HTTP Servlet Request 
     * @param body  The body of the request 
     * @return ResponseDTO  A DTO representing the response 
     */
    @PutMapping(value = "/replace",  produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Replace a document", description = "Servizio che consente di effettuare la replace di un Document.")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ReplaceResponseDTO.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replace Document avvenuta con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReplaceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ReplaceResponseDTO replace(@RequestBody FhirPublicationDTO body,HttpServletRequest request);
    
    /** 
     * Check if the document exists on the secondary storage (MongoDB). 
     * 
     * @param id  The ID of the document to search 
     * @param request  The HTTP Servlet Request 
     * @return ResourceExistResDTO  A DTO with a boolean, representing whether the resource already exists 
     */
    @GetMapping(value = "/check-exist/{id}")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceExistResDTO.class)))
	@Operation(summary = "Controllo esistenza risorsa", description = "Controlla se su Elasticsearch è presente una risorsa con l'id fornito.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResourceExistResDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    ResourceExistResDTO exist(@PathVariable(required = true, name = "id") String id, HttpServletRequest request);

    
    
    
    /** 
     * Updates an existing document on FHIR Server. 
     * 
     * @param request  The HTTP Servlet Request 
     * @param body  The body of the request 
     * @return ResponseDTO  A DTO representing the response 
     */
    @PutMapping(value = "/metadata/{identifier}",  produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Update Document metadata", description = "Servizio che consente di aggiornare i metadati di un Documento.")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = UpdateResponseDTO.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aggiornamento metadati completato con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UpdateResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) }) 
    UpdateResponseDTO updateMetadata(FhirPublicationDTO body,HttpServletRequest request);
    
}
