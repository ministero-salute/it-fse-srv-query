package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DiffResult {

    private final Date currentTime;

    private final Date lastUpdate;

    private final Map<String, DiffResource> changeset;

    public Date currentTime() {
        return currentTime;
    }

    public Date lastUpdate() {
        return lastUpdate;
    }

    public Map<String, DiffResource> changeset() {
        return new HashMap<>(changeset);
    }

    public boolean isEmpty() {
        return changeset.isEmpty();
    }
}
