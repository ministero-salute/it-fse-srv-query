package it.finanze.sanita.fse2.ms.srvquery.controller;

import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
 

@RequestMapping(path = "/v1/subscription")
@Tag(name = "Servizio gestione subscription fhir")
@Validated
public interface ISubscriptionCTL {

	@PostMapping(value = "")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = void.class)))
	@Operation(summary = "Controllo esistenza risorsa", description = "Controlla se su Elasticsearch è presente una risorsa con l'id fornito.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = void.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	void manageSubscription(@RequestParam SubscriptionEnum target, @RequestParam SubscriptionStatus status);



}

