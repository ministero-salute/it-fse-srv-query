package it.finanze.sanita.fse2.ms.srvquery.dto.response.history;

import it.finanze.sanita.fse2.ms.srvquery.enums.history.HistoryOperationEnum;
import lombok.Value;
import org.hl7.fhir.r4.model.ResourceType;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus;

@Value
public class RawHistoryDTO {

    Date currentTime;
    Date lastUpdate;

    Map<String, HistoryDetailsDTO> history;

    @Value
    public static class HistoryDetailsDTO {

        ResourceType type;
        String version;
        HistoryOperationEnum op;
        PublicationStatus status;
        @Nullable
        Date lastUpdated;

        public static HistoryDetailsDTO from(HistoryDetailsDTO dto, HistoryOperationEnum op) {
            return new HistoryDetailsDTO(
                dto.type,
                dto.version,
                op,
                dto.status,
                dto.lastUpdated
            );
        }
    }
}
