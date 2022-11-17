package it.finanze.sanita.fse2.ms.srvquery.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateBodyDTO {
	 
	private String tipologiaStruttura;

	private List<String> attiCliniciRegoleAccesso;
	
	private String tipoDocumentoLivAlto;

	private String assettoOrganizzativo;
	 
	private String dataInizioPrestazione;

	private String dataFinePrestazione;

	private String conservazioneANorma;

	private String tipoAttivitaClinica;

	private String identificativoSottomissione;
}

