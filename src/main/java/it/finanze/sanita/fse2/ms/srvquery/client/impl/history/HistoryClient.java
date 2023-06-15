package it.finanze.sanita.fse2.ms.srvquery.client.impl.history;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import it.finanze.sanita.fse2.ms.srvquery.config.TerminologyCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static ca.uhn.fhir.rest.api.CacheControlDirective.noCache;
import static ca.uhn.fhir.rest.api.SummaryEnum.TRUE;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO.*;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;
import static org.springframework.http.HttpMethod.POST;

@Component
public class HistoryClient {

    private static final int CHUNK_SIZE = 10;
    private final IGenericClient client;

    public HistoryClient(@Autowired TerminologyCFG cfg) {
        this.client = createClient(
            cfg.getFhirServerUrl(),
            cfg.getFhirServerUser(),
            cfg.getFhirServerPwd()
        );
    }

    public HistoryDTO getHistory(Date lastUpdate) {
        return findByLastUpdate(lastUpdate);
    }

    public Map<String, HistoryDetailsDTO> getHistoryMap(Date lastUpdate) {
        return findByLastUpdate(lastUpdate).getHistory();
    }

    private HistoryDTO findByLastUpdate(Date lastUpdate) {
        HistoryDTO out;
        if(lastUpdate == null) {
            out = findAny();
        } else {
            out = findModifiedByDate(lastUpdate);
        }
        return out;
    }

    private HistoryDTO findAny() {
        // Execute query by resource op and last-update date
        Bundle bundle = client
            .search()
            .forResource(CodeSystem.class)
            .cacheControl(noCache())
            .returnBundle(Bundle.class)
            .count(CHUNK_SIZE)
            .summaryMode(TRUE)
            .execute();
        // Create composer
        HistoryComposer composer = new HistoryComposer(client, bundle);
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new HistoryDTO(
            currentTime,
            null,
            composer.compose(POST, null)
        );
    }

    private HistoryDTO findModifiedByDate(Date lastUpdate) {
        // Execute query by resource op and last-update date
        Bundle bundle = client
            .history()
            .onType(CodeSystem.class)
            .returnBundle(Bundle.class)
            .cacheControl(noCache())
            .since(lastUpdate)
            .summaryMode(TRUE)
            .count(CHUNK_SIZE)
            .execute();
        // Create composer
        HistoryComposer composer = new HistoryComposer(client, bundle);
        // Get current time
        Date currentTime = bundle.getMeta().getLastUpdated();
        // Retrieve resources
        return new HistoryDTO(
            currentTime,
            lastUpdate,
            composer.compose(null, lastUpdate)
        );
    }

    public void resetFhir() {
        // Inside terminology-server (AppProperties.java):
        // allow_multiple_delete = true
        client
            .operation()
            .onServer()
            .named("$expunge")
            .withParameter(
                Parameters.class, "expungeEverything", new BooleanType(true)
            ).execute();
    }

}
