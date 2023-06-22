package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.*;

@Getter
@JsonInclude(NON_NULL)
public class HistoryResDTO {

    private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private String traceID;
    private String spanID;

    @JsonFormat(pattern = PTT_ISO_8601)
    private final Date timestamp;
    @JsonFormat(pattern = PTT_ISO_8601)
    private final Date lastUpdate;

    private final List<HistoryInsertDTO> insertions;
    private final List<HistoryDeleteDTO> deletions;

    public HistoryResDTO(Date timestamp, Date lastUpdate) {
        this.timestamp = timestamp;
        this.lastUpdate = lastUpdate;
        this.insertions = new ArrayList<>();
        this.deletions = new ArrayList<>();
    }

    public HistoryResDTO trackWith(LogTraceInfoDTO info) {
        this.traceID = info.getTraceID();
        this.spanID = info.getSpanID();
        return this;
    }

    @Value
    public static class HistoryInsertDTO {
        String id;
        String version;
        String type;

        public static HistoryInsertDTO from(String id, HistoryDetailsDTO dto) {
            return new HistoryInsertDTO(id, dto.getVersion(), dto.getType().getPath());
        }

    }

    @Value
    @JsonInclude(NON_NULL)
    public static class HistoryDeleteDTO {
        String id;
        String type;
        String omit;

        public static HistoryDeleteDTO from(String id, HistoryDetailsDTO dto) {
            return new HistoryDeleteDTO(id, dto.getType().getPath(), dto.getVersion());
        }

        public static HistoryDeleteDTO fromThenOmit(String id, HistoryDetailsDTO dto) {
            return new HistoryDeleteDTO(id, dto.getType().getPath(), null);
        }

    }

}
