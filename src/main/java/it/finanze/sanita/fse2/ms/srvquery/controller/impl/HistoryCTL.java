package it.finanze.sanita.fse2.ms.srvquery.controller.impl;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.history.HistoryClient;
import it.finanze.sanita.fse2.ms.srvquery.controller.AbstractCTL;
import it.finanze.sanita.fse2.ms.srvquery.controller.IHistoryCTL;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@Slf4j
public class HistoryCTL extends AbstractCTL implements IHistoryCTL {

    @Autowired
    private HistoryClient client;

    @Override
    public HistoryDTO history(Date lastUpdate) {
        return client.getHistory(lastUpdate);
    }
}
