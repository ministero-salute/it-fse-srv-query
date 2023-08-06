package it.finanze.sanita.fse2.ms.srvquery.dto;

import it.finanze.sanita.fse2.ms.srvquery.enums.ResultPushEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataResourceDTO {

	private String system;
	
	private String url;
	
	private ResultPushEnum esito;
}
