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

    private final String resourceId;
    private final String versionId;
    private final ResourceMetaDTO meta;
    private final List<ResourceItemDTO> items;

    public HistoryResourceResDTO(String resourceId, String versionId, ResourceMetaDTO meta, List<ResourceItemDTO> items) {
        this.resourceId = resourceId;
        this.versionId = versionId;
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
        String oid;
        String version;
        String type;
        @JsonFormat(pattern = PTT_ISO_8601)
        Date released;
        boolean whitelist;
    }

    @Value
    @JsonInclude(NON_NULL)
    public static class ResourceItemDTO {
        String code;
        String display;
    }

}
