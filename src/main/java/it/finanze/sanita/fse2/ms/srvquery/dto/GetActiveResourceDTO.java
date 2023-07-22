package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class GetActiveResourceDTO {
	private String oid;
	private String id;
	private String version;
	private Date lastUpdate;
}
