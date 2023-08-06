package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class GetActiveResourceDTO {

	private String id;
	
	private String oid;
}
