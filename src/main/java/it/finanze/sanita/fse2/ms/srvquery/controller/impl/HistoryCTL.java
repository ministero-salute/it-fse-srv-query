package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IHistoryCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistorySnapshotDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.service.IHistorySRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@Slf4j
public class HistoryCTL extends AbstractCTL implements IHistoryCTL {

    @Autowired
    private IHistorySRV service;

    @Override
    public HistorySnapshotDTO snapshot() {
        return service.snapshot().trackWith(getLogTraceInfo());
    }

    @Override
    public HistoryResDTO history(Date lastUpdate) {
        return service.history(lastUpdate).trackWith(getLogTraceInfo());
    }

    @Override
    public HistoryResourceResDTO resource(String resourceId, String versionId) throws ResourceNotFoundException, MalformedResourceException {
        return service.resource(resourceId, versionId).trackWith(getLogTraceInfo());
    }
}
