package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IHistoryCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
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
    public HistoryDTO history(Date lastUpdate) {
        return service.history(lastUpdate);
    }

    @Override
    public HistoryResourceDTO resource(String resourceId, String versionId) throws ResourceNotFoundException, MalformedResourceException {
        return service.resource(resourceId, versionId);
    }
}