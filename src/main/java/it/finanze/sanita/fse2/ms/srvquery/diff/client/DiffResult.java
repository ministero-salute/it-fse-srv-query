package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DiffResult {

    private final Date currentTime;

    private final Date lastUpdate;

    private final Map<String, DiffOpType> ids;

    public Date currentTime() {
        return currentTime;
    }

    public Date lastUpdate() {
        return lastUpdate;
    }

    public List<String> ids() {
        return new ArrayList<>(ids.keySet());
    }

    public Map<String, DiffOpType> mapping() {
        return new HashMap<>(ids);
    }

}
