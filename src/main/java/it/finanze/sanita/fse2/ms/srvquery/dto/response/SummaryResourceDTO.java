package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.finanze.sanita.fse2.ms.srvquery.enums.MetadataResourceTypeEnum;
import it.finanze.sanita.fse2.ms.srvquery.service.impl.TerminologySRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.MetadataUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;

@Data
@NoArgsConstructor
public class SummaryResourceDTO {
	
	private static final String PTT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private String resourceId;
	
	private String oid;
	
	private String version;
	
	private MetadataResourceTypeEnum metadataType;
	
	private String status;
	
	private String url;
	
	private String content;
	
	@JsonFormat(pattern = PTT_ISO_8601)
	private Date lastUpdated;
	
	private boolean exportable;

	public static SummaryResourceDTO fromResource(MetadataResource data) {
		SummaryResourceDTO res = new SummaryResourceDTO();
		Optional<String> oid = MetadataUtility.hasOID(data);
		res.setResourceId(data.getIdElement().getIdPart());
		res.setStatus(data.hasStatusElement() ? data.getStatusElement().asStringValue() : "");
		res.setUrl(data.getUrl());
		res.setVersion(data.getVersion());
		res.setLastUpdated(data.getMeta().getLastUpdated());
		res.setExportable(TerminologySRV.isExportable(data));
		res.setMetadataType(MetadataResourceTypeEnum.fromFhirType(data.fhirType()));
		oid.ifPresent(s -> res.setOid(StringUtility.removeUrnOidFromSystem(s)));
		if(data instanceof CodeSystem) {
			CodeSystem cs = (CodeSystem) data;
			if(cs.getContent() != null) res.setContent(cs.getContent().getDisplay());
		}
		return res;
	}
	
}
