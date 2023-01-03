/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto;

import java.util.List;

import lombok.*;

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

