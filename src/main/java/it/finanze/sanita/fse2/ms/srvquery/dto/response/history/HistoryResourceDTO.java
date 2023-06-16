package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@Value
@JsonInclude(NON_NULL)
public class HistoryResourceDTO {

    String id;
    String version;
    ResourceMetaDTO meta;
    List<ResourceItemDTO> items;

    @Value
    @JsonInclude(NON_NULL)
    public static class ResourceMetaDTO {
        String resourceId;
        String versionId;
        String resourceType;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        Date releaseDate;
    }

    @Value
    @JsonInclude(NON_NULL)
    public static class ResourceItemDTO {
        String code;
        String display;
    }

}
