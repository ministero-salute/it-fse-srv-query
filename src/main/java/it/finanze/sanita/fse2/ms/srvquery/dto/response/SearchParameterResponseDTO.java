package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import it.finanze.sanita.fse2.ms.srvquery.dto.ResourceSearchParameterDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchParameterResponseDTO {

	private List<ResourceSearchParameterDTO> params;

}
