package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder
@EqualsAndHashCode(callSuper=true)
public class KafkaCreateDTO extends AbstractDTO{

    private String resource;
    
}
