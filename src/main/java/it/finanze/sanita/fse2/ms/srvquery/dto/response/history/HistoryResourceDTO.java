package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import lombok.Value;

import java.util.List;

@Value
public class HistoryResourceDTO {

    String id;
    String version;
    ResourceMetaDTO meta;
    List<ResourceItemDTO> items;

    @Value
    public static class ResourceMetaDTO {
        String resourceId;
        String versionId;
        String resourceType;
    }

    @Value
    public static class ResourceItemDTO {
        String code;
        String display;
    }

}
