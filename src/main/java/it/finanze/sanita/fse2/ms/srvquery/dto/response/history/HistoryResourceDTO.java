package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import lombok.Value;

@Value
public class HistoryResourceDTO {

    public final static String NO_VERSION = "-";

    String version;
    HistoryOperationEnum op;
}
