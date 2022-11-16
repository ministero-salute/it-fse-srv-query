package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateResponseDTO {

	private boolean esito;
	
	private String message;
}
