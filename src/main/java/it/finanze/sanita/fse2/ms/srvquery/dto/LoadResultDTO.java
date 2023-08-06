package it.finanze.sanita.fse2.ms.srvquery.dto;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoadResultDTO {

	private Date startInsert;
	private Date stopInsert;
	private Long durationInsertMS;
	
	private Date startRead;
	private Date stopRead;
	private Long durationReadMS;
	
	private Long sampleSize;

	private Boolean result;
	private String msg;

}
