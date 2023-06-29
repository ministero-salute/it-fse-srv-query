package it.finanze.sanita.fse2.ms.srvquery.service;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistorySnapshotDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;

import java.util.Date;

public interface IHistorySRV {
    HistorySnapshotDTO snapshot();
    HistoryResDTO history(Date lastUpdate);
    HistoryResourceResDTO resource(String resourceId, String versionId) throws ResourceNotFoundException, MalformedResourceException;
}
