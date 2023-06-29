package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.RawHistoryDTO.HistoryDetailsDTO;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonInclude(NON_NULL)
public class HistorySnapshotDTO {

    private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private String traceID;
    private String spanID;

    @JsonFormat(pattern = PTT_ISO_8601)
    private final Date timestamp;

    private final List<Resources> resources;

    public HistorySnapshotDTO(Date timestamp, List<Resources> resources) {
        this.timestamp = timestamp;
        this.resources = resources;
    }

    public static HistorySnapshotDTO from(Date lastUpdate, Map<String, HistoryDetailsDTO> compose) {
        List<Resources> res = new ArrayList<>();
        for (Map.Entry<String, HistoryDetailsDTO> entry : compose.entrySet()) {
            String id = entry.getKey();
            HistoryDetailsDTO details = entry.getValue();
            res.add(new Resources(id, details.getVersion(), details.getType().getPath()));
        }
        return new HistorySnapshotDTO(lastUpdate, res);
    }

    @Value
    public static class Resources {
        String id;
        String version;
        String type;
    }

    public HistorySnapshotDTO trackWith(LogTraceInfoDTO info) {
        this.traceID = info.getTraceID();
        this.spanID = info.getSpanID();
        return this;
    }

}
