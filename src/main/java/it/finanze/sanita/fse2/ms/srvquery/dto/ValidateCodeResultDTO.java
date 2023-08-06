package it.finanze.sanita.fse2.ms.srvquery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidateCodeResultDTO {
	private Boolean result;
	private String msg;
}