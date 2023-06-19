package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import lombok.Value;

import java.util.Date;
import java.util.Map;

@Value
public class RawHistoryDTO {

    Date currentTime;
    Date lastUpdate;

    Map<String, HistoryDetailsDTO> history;

    @Value
    public static class HistoryDetailsDTO {
        public final static String ANY_VERSION = "*";
        String version;
        HistoryOperationEnum op;
    }
}
