package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class DiffResult {

    private final Date currentTime;

    private final Date lastUpdate;

    private final List<String> ids;

    public Date currentTime() {
        return currentTime;
    }

    public Date lastUpdate() {
        return lastUpdate;
    }

    public List<String> ids() {
        return ids;
    }
}
