package it.finanze.sanita.fse2.ms.srvquery.dto;

import it.finanze.sanita.fse2.ms.srvquery.enums.TypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDTO {
	private String name;
	private String version;
	private String url;
	private String oid;
	private TypeEnum type;
}
