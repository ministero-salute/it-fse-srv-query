package it.finanze.sanita.fse2.ms.srvquery.client.impl.history;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryAbstractClient;
import it.finanze.sanita.fse2.ms.srvquery.config.TerminologyCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO.HistoryDetailsDTO;
import static it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper.createClient;

@Component
public class HistoryClient extends HistoryAbstractClient {

    public HistoryClient(@Autowired TerminologyCFG cfg) {
        super(
            createClient(
                cfg.getFhirServerUrl(),
                cfg.getFhirServerUser(),
                cfg.getFhirServerPwd()
            )
        );
    }

    public HistoryDTO getHistory(Date lastUpdate) {
        return createHistoryByLastUpdate(lastUpdate);
    }

    public Map<String, HistoryDetailsDTO> getHistoryMap(Date lastUpdate) {
        return createHistoryByLastUpdate(lastUpdate).getHistory();
    }

    public Optional<HistoryResourceDTO> getResource(String resourceId, String versionId) throws MalformedResourceException {
        return getMappedResource(resourceId, versionId);
    }

    public void resetFhir() {
        reset();
    }

}
