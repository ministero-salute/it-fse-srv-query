package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.HistoryClient;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.service.IHistorySRV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO.*;

@Service
public class HistorySRV implements IHistorySRV {

    @Autowired
    private HistoryClient client;

    @Override
    public HistoryDTO history(Date lastUpdate) {
        return convert(client.getHistory(lastUpdate));
    }

    @Override
    public HistoryResourceDTO resource(String resourceId, String versionId) throws ResourceNotFoundException, MalformedResourceException {
        Optional<HistoryResourceDTO> resource = client.getResource(resourceId, versionId);
        if(!resource.isPresent()) {
            throw new ResourceNotFoundException(resourceId, versionId);
        }
        return resource.get();
    }

    private HistoryDTO convert(RawHistoryDTO history) {
        // Working var
        HistoryDTO out = new HistoryDTO(
            history.getCurrentTime(),
            history.getLastUpdate()
        );
        // Iterate and map
        Map<String, HistoryDetailsDTO> map = history.getHistory();
        // On each entry
        map.forEach((id, details) -> {
            // Map by operation type
            switch (details.getOp()) {
                case INSERT:
                    out.getInsertions().add(new HistoryInsertDTO(id, details.getVersion()));
                    break;
                case UPDATE:
                    // Define as insertion the newest available version
                    out.getInsertions().add(new HistoryInsertDTO(id, details.getVersion()));
                    // Instruct to delete anything else but omit the latest inserted
                    out.getDeletions().add(new HistoryDeleteDTO(id, details.getVersion()));
                    break;
                case DELETE:
                    // Instruct to delete the whole resource, nothing to omit here
                    out.getDeletions().add(new HistoryDeleteDTO(id, null));
                    break;
            }
        });
        // Return mapped resource
        return out;
    }

}
