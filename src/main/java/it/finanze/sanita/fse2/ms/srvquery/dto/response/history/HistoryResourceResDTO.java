package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import lombok.Getter;
import lombok.Value;

import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@Getter
@JsonInclude(NON_NULL)
public class HistoryResourceResDTO {

    private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private String traceID;
    private String spanID;

    private final String id;
    private final String version;
    private final ResourceMetaDTO meta;
    private final List<ResourceItemDTO> items;

    public HistoryResourceResDTO(String id, String version, ResourceMetaDTO meta, List<ResourceItemDTO> items) {
        this.id = id;
        this.version = version;
        this.meta = meta;
        this.items = items;
        this.traceID = "";
        this.spanID = "";
    }

    public HistoryResourceResDTO trackWith(LogTraceInfoDTO info) {
        this.traceID = info.getTraceID();
        this.spanID = info.getSpanID();
        return this;
    }

    @Value
    @JsonInclude(NON_NULL)
    public static class ResourceMetaDTO {
        String resourceId;
        String versionId;
        String resourceType;
        @JsonFormat(pattern = PTT_ISO_8601)
        Date releaseDate;
    }

    @Value
    @JsonInclude(NON_NULL)
    public static class ResourceItemDTO {
        String code;
        String display;
    }

}
