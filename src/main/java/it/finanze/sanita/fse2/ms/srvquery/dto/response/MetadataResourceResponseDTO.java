package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import org.hl7.fhir.r4.model.ValueSet;

import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataResourceResponseDTO extends ResponseDTO {

	private List<MetadataResourceDTO> metadataResource;

	public MetadataResourceResponseDTO(final LogTraceInfoDTO traceInfo, final List<MetadataResourceDTO> inMetadataResource) {
		super(traceInfo);
		metadataResource = inMetadataResource;
	}
	
//	public MetadataResourceResponseDTO(final LogTraceInfoDTO traceInfo, final List<ValueSet> valuesets) {
//		super(traceInfo);
//		for (ValueSet valueSet : valuesets) {
//			MetadataResourceDTO dto = new MetadataResourceDTO();
//			dto.setSystem(valueSet.get);
//			dto.setUrl(valueSet.getUrl());
//		}
//		metadataResource.addAll();
//	}
}