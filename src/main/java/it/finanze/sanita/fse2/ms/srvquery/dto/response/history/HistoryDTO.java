package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.util.Date;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@Value
@JsonInclude(NON_NULL)
public class HistoryDTO {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Date currentTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Date lastUpdate;
    Map<String, HistoryResourceDTO> history;
}
