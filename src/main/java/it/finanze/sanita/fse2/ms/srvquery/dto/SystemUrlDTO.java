package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to map system url.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemUrlDTO {

	private String system;
	
	private String url;
	
	private boolean toDelete;
 
	private Boolean forceDraft;

}
