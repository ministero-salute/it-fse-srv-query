package it.finanze.sanita.fse2.ms.srvquery.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceSearchParameterDTO {

	private String resourceName;
	private List<SearchParameterDTO> parameters;
		
}
