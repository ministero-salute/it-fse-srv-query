package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.finanze.sanita.fse2.ms.srvquery.enums.MetadataResourceTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SummaryResourceDTO {
	
	private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private String resourceId;
	
	private String oid;
	
	private String version;
	
	private MetadataResourceTypeEnum metadataType;
	
	private String status;
	
	private String url;
	
	private String content;
	
	@JsonFormat(pattern = PTT_ISO_8601)
	private Date lastUpdated;
	
	private boolean exportable;
	
}
