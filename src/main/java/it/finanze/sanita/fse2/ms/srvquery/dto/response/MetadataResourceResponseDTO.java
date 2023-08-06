package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

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
}