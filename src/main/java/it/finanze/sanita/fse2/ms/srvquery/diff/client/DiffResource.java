package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiffResource {

    public final static String NO_VERSION = "-";

    private final String version;
    private final DiffOpType type;

    public String version() {
        return version;
    }

    public DiffOpType op() {
        return type;
    }

}
