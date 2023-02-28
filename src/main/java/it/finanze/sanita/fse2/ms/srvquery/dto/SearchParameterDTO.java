package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchParameterDTO {

	private String name;
	private String type; 
	private String path;
	
}
