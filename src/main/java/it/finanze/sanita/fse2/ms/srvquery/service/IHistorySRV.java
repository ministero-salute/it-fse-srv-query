package it.finanze.sanita.fse2.ms.srvquery.service;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;

import java.util.Date;

public interface IHistorySRV {
    HistoryDTO history(Date lastUpdate);
    HistoryResourceDTO resource(String resourceId, String versionId) throws ResourceNotFoundException, MalformedResourceException;
}
