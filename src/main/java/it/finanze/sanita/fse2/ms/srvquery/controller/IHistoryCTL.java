package it.finanze.sanita.fse2.ms.srvquery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.error.base.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.validators.NoFutureDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;
import java.util.Date;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Logs.*;
import static it.finanze.sanita.fse2.ms.srvquery.utility.RoutesUtility.*;
import static org.springframework.format.annotation.DateTimeFormat.ISO.*;

@Tag(name = API_HISTORY_TAG)
@Validated
public interface IHistoryCTL {

    @GetMapping(value = API_GET_HISTORY)
    @Operation(
        summary = "Retrieve history by last-update",
        description = "Returns an on-the-fly history status for the given timeframe"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Storico recuperato",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HistoryDTO.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "I parametri forniti non sono validi",
                content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class))
            )
        }
    )
    HistoryDTO history(
        @RequestParam(value=API_QP_LAST_UPDATE, required = false)
        @DateTimeFormat(iso = DATE_TIME)
        @NoFutureDate(message = ERR_VAL_FUTURE_DATE)
        Date lastUpdate
    );

    @GetMapping(API_GET_RESOURCE_HISTORY)
    @Operation(
        summary = "Retrieve resource by id and version-id using a compatibility layer",
        description = "Returns CS or VS by id and version-id using a compatibility layer"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Risorsa recuperata",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HistoryResourceDTO.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "I parametri forniti non sono validi",
                content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDTO.class))
            )
        }
    )
    HistoryResourceDTO resource(
        @PathVariable(API_PATH_RES_ID_VAR)
        @NotBlank(message = ERR_VAL_RESOURCE_ID_BLANK)
        String resourceId,
        @PathVariable(API_PATH_RES_VERSION_VAR)
        @NotBlank(message = ERR_VAL_VERSION_ID_BLANK)
        String versionId
    ) throws ResourceNotFoundException, MalformedResourceException;

}