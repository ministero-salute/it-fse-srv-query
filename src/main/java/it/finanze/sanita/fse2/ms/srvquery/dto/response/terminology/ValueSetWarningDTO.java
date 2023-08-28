package it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology;

import java.util.List;

import org.hl7.fhir.r4.model.ValueSet;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ResponseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValueSetWarningDTO extends ResponseDTO {

	private List<ValueSet> valuesets;
	
	public ValueSetWarningDTO(final LogTraceInfoDTO traceInfo, final List<ValueSet> valuesets) {
		super(traceInfo);
		this.valuesets = valuesets;
	}
}
