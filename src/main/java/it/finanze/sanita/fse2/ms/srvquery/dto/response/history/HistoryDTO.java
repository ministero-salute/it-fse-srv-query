package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import lombok.Value;

import java.util.Date;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@Value
@JsonInclude(NON_NULL)
public class HistoryDTO {

    private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @JsonFormat(pattern = PTT_ISO_8601)
    Date currentTime;
    @JsonFormat(pattern = PTT_ISO_8601)
    Date lastUpdate;

    Map<String, HistoryDetailsDTO> history;

    @Value
    public static class HistoryDetailsDTO {
        public final static String NO_VERSION = "-";
        String version;
        HistoryOperationEnum op;
    }
}
