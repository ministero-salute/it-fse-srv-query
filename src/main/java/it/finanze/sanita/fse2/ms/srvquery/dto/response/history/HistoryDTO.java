package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import lombok.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@JsonInclude(NON_NULL)
public class HistoryDTO {

    private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @JsonFormat(pattern = PTT_ISO_8601)
    Date currentTime;
    @JsonFormat(pattern = PTT_ISO_8601)
    Date lastUpdate;

    List<HistoryInsertDTO> insertions;
    List<HistoryDeleteDTO> deletions;

    public HistoryDTO(Date currentTime, Date lastUpdate) {
        this.currentTime = currentTime;
        this.lastUpdate = lastUpdate;
        this.insertions = new ArrayList<>();
        this.deletions = new ArrayList<>();
    }

    @Value
    public static class HistoryInsertDTO {
        String id;
        String version;
    }

    @Value
    @JsonInclude(NON_NULL)
    public static class HistoryDeleteDTO {
        String id;
        String omit;
    }

}
