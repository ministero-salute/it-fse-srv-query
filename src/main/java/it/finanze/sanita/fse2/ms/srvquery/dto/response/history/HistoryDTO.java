package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import lombok.Value;

import java.util.Date;
import java.util.Map;

@Value
public class HistoryDTO {
    Date currentTime;
    Date lastUpdate;
    Map<String, HistoryResourceDTO> history;
}
