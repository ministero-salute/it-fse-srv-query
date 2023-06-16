package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.HistoryClient;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.service.IHistorySRV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class HistorySRV implements IHistorySRV {

    @Autowired
    private HistoryClient client;

    @Override
    public HistoryDTO history(Date lastUpdate) {
        return client.getHistory(lastUpdate);
    }

    @Override
    public HistoryResourceDTO resource(String resourceId, String versionId) throws ResourceNotFoundException, MalformedResourceException {
        Optional<HistoryResourceDTO> resource = client.getResource(resourceId, versionId);
        if(!resource.isPresent()) {
            throw new ResourceNotFoundException(resourceId, versionId);
        }
        return resource.get();
    }

}
