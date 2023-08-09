package it.finanze.sanita.fse2.ms.srvquery.dto;

import it.finanze.sanita.fse2.ms.srvquery.enums.MetadataResourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class GetActiveResourceDTO {

	private String id;
	
	private String oid;
	
	private MetadataResourceTypeEnum metadataType;
}
