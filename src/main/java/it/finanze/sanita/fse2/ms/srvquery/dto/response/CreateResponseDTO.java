package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateResponseDTO {

	private boolean esito;
	
	private String message;
}