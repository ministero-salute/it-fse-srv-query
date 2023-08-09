package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import it.finanze.sanita.fse2.ms.srvquery.enums.MetadataResourceTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SummaryResourceDTO {

	private String resourceId;
	
	private String oid;
	
	private String version;
	
	private MetadataResourceTypeEnum metadataType;
	
	private String status;
	
	private String url;
	
	private String content;
	
}
