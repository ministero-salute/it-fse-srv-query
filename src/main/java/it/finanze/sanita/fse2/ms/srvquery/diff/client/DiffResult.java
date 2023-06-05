package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DiffResult {

    private final Date currentTime;

    private final Date lastUpdate;

    private final Map<String, DiffOpType> changeset;

    public Date currentTime() {
        return currentTime;
    }

    public Date lastUpdate() {
        return lastUpdate;
    }

    public Map<String, DiffOpType> changeset() {
        return new HashMap<>(changeset);
    }

    public boolean isEmpty() {
        return changeset.isEmpty();
    }
}
