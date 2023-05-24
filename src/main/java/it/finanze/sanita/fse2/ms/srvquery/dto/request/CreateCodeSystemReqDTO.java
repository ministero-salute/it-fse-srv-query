package it.finanze.sanita.fse2.ms.srvquery.dto.request;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCodeSystemReqDTO {

	private String oid;
	private String name;
	private String version;
	private List<CodeDTO> codes;
	
}
