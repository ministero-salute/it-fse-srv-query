package it.finanze.sanita.fse2.ms.srvquery.utility;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@NoArgsConstructor(access = PRIVATE)
public final class RoutesUtility {

    public static final String API_VERSION = "v1";
    public static final String API_HISTORY = "history";

    public static final String API_QP_LAST_UPDATE = "lastUpdate";

    public static final String API_GET_HISTORY = "/" + API_VERSION + "/" + API_HISTORY;

    public static final String API_HISTORY_TAG = "Servizio storico FHIR";

}
