package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Resource.SECURITY_CODE_NORMAL;
import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Resource.SECURITY_SYSTEM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.client.IConverterClient;
import it.finanze.sanita.fse2.ms.srvquery.client.IWebScrapingClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.config.TerminologyCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.GetActiveResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.ResultPushEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.TypeEnum;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Service Implementation 
 */
@Service
@Slf4j
public class TerminologySRV implements ITerminologySRV {

	@Autowired
	private TerminologyCFG terminologyCFG;

	private TerminologyClient terminologyClient;

	@Autowired
	private IConverterClient converter;

	@Autowired
	private IWebScrapingClient client;

	private TerminologyClient getTerminologyClient() {
		if (terminologyClient == null) {
			synchronized (this) {
				if (terminologyClient == null) {
					terminologyClient = new TerminologyClient(terminologyCFG.getFhirServerUrl(), terminologyCFG.getFhirServerUser(), terminologyCFG.getFhirServerPwd());
				}
			}
		}
		return terminologyClient;
	}

	@Override
	public void manageSubscription(SubscriptionEnum subscriptionEnum, SubscriptionStatus actionEnum) {
		TerminologyClient terminologyClient = getTerminologyClient();

		if(SubscriptionEnum.ALL.equals(subscriptionEnum)) {
			for(SubscriptionEnum s : SubscriptionEnum.values()) {
				if(SubscriptionEnum.ALL.equals(s)) {
					continue;
				}
				terminologyClient.manageSubscription(s,actionEnum,terminologyCFG.getPolicyManagerUrl());		
			}
		} else {
			terminologyClient.manageSubscription(subscriptionEnum,actionEnum,terminologyCFG.getPolicyManagerUrl());
		}
	}

	@Override
	public String insertCodeSystem(String name, String oid, String version, List<CodeDTO> codes) {
		TerminologyClient terminologyClient = getTerminologyClient();
		return terminologyClient.insertCS(oid, name, version, codes);
	}


	@Override
	public CreateCodeSystemResDTO manageCodeSystem(final CreateCodeSystemReqDTO dto) {
		CreateCodeSystemResDTO out = new CreateCodeSystemResDTO();

		CodeSystem codeSystem = getCodeSystemById("urn:oid:"+dto.getOid());

		if(codeSystem==null) {
			String id = insertCodeSystem(dto.getName(), dto.getOid(), dto.getVersion(), dto.getCodes()); 
			out.setId(id);
		} else if(PublicationStatus.DRAFT.equals(codeSystem.getStatusElement().getValue())) {
			updateCodeSystem(codeSystem, dto.getCodes());
			out.setId(codeSystem.getId());
		}

		return out;
	}

	private CodeSystem getCodeSystemById(final String oid) {
		TerminologyClient terminologyClient = getTerminologyClient();
		return terminologyClient.getCodeSystemById(oid);
	}

	private void updateCodeSystem(final CodeSystem codeSystem, List<CodeDTO> codes) {
		TerminologyClient terminologyClient = getTerminologyClient();
		terminologyClient.updateCS(codeSystem, codes);
	}

	@Override
	public List<MetadataResourceDTO> manageMetadataResource(final List<SystemUrlDTO> list) {
		TerminologyClient terminologyClient = getTerminologyClient();
		List<MetadataResourceDTO> out = new ArrayList<>();		

		for(SystemUrlDTO entry : list) {
			ResultPushEnum esito = null;

			String res = client.webScraper(entry.getUrl());
			if(StringUtility.isNullOrEmpty(res)) {
				esito = ResultPushEnum.RESOURCE_NOT_FOUND;
				out.add(new MetadataResourceDTO(entry.getSystem(),entry.getUrl(),  esito));
				continue;
			}
			esito = terminologyClient.handlePullMetadataResource(res, entry.getForceDraft());
			out.add(new MetadataResourceDTO(entry.getSystem(), entry.getUrl(),esito));
		}
		return out;
	}

	@Override
	public UploadResponseDTO uploadTerminology(FormatEnum formatEnum,RequestDTO creationInfo, MultipartFile file) throws IOException {
		log.info("Upload terminology with format:" + formatEnum);
		TerminologyClient terminologyClient = getTerminologyClient();

		String fhirBundle = new String(file.getBytes() ,StandardCharsets.UTF_8);
		if(!FormatEnum.FHIR_R4_JSON.equals(formatEnum)) {
			if(creationInfo == null) {
				creationInfo = new RequestDTO();
				creationInfo.setType(TypeEnum.CODE_SYSTEM);
			}
			ConversionResponseDTO res = converter.callConvertToFhirJson(formatEnum,creationInfo,file);
			fhirBundle = res.getResult();
		}

		CodeSystem codeSystem = FHIRR4Helper.deserializeResource(CodeSystem.class, fhirBundle, true);
		String location = terminologyClient.transaction(codeSystem);

		UploadResponseDTO out = new UploadResponseDTO();
		out.setLocation(location);
		out.setInsertedItems(codeSystem.getConcept().size());
		return out;
	}


	@Override
	public GetResponseDTO isPresent(String oid, String version) {
		GetResponseDTO out = new GetResponseDTO(); 
		TerminologyClient terminologyClient = getTerminologyClient();
		CodeSystem codeSystem = terminologyClient.getCodeSystemByIdAndVersion(oid, version);
		out.setPresent(codeSystem!=null);
		out.setCounter(codeSystem!=null ? codeSystem.getCount() : null);
		out.setId(codeSystem!=null ? codeSystem.getIdElement().getIdPartAsLong().toString() : null);
		return out;
	}

	@Override
	public void deleteById(String id) {
		TerminologyClient terminologyClient = getTerminologyClient();
		terminologyClient.deleteCS(id);
	}

	@Override
	public List<GetActiveResourceDTO> getSummaryNameActiveResource() {
		List<GetActiveResourceDTO> out = new ArrayList<>();
		TerminologyClient terminologyClient = getTerminologyClient();
		List<CodeSystem> codeSystems = terminologyClient.searchSummaryNames();
		List<ValueSet> valueSets = terminologyClient.searchActiveValueSet();
		for(CodeSystem codeSystem : codeSystems) {
			String id = codeSystem.getIdElement().getIdPartAsLong().toString();
			if(!codeSystem.getIdentifier().isEmpty() && !codeSystem.getVersion().isEmpty()) {
				String oid = StringUtility.removeUrnOidFromSystem(codeSystem.getIdentifier().get(0).getValue());
				String version = codeSystem.getVersion();
				out.add(new GetActiveResourceDTO(id, oid, version));
			}
		}
		for(ValueSet valueSet : valueSets) {
			String id = valueSet.getIdElement().getIdPartAsLong().toString();
			if(!valueSet.getIdentifier().isEmpty() && !valueSet.getVersion().isEmpty()) {
				String oid = StringUtility.removeUrnOidFromSystem(valueSet.getIdentifier().get(0).getValue());
				String version = valueSet.getVersion();
				out.add(new GetActiveResourceDTO(id, oid, version));
			}
		}
		return out;
	}

	@Override
	public GetResDTO export(String id, FormatEnum format) {
		GetResDTO out = new GetResDTO();
		TerminologyClient terminologyClient = getTerminologyClient();
		Optional<IBaseResource> base = terminologyClient.getResource(id);

		if(base.isPresent()) {
			IBaseResource res = base.get();
			if (res instanceof CodeSystem) {
				CodeSystem codeSystem = (CodeSystem) res;
				out = setContentCS(codeSystem, format);
			} else if (res instanceof ValueSet) {
				ValueSet valueset = (ValueSet) res;
				out = setContentVS(valueset, format);
			}
		}
		
		return out;
	}

	private GetResDTO setContentCS(CodeSystem codeSystem, FormatEnum format) {
		GetResDTO out = new GetResDTO();

		String resource = FHIRR4Helper.serializeResource(codeSystem, true, true, false);
		String oid = StringUtility.removeUrnOidFromSystem(codeSystem.getIdentifier().get(0).getValue());
		out.setOid(oid);
		out.setExportable(false);

		if(codeSystem.getMeta().getSecurity()==null || 
			codeSystem.getMeta().getSecurity().isEmpty() ||
			codeSystem.getMeta().getSecurity(SECURITY_SYSTEM, SECURITY_CODE_NORMAL)!=null) {
			try {
				out.setExportable(true);
				out.setContent(createByteContent(resource, oid, format));
			} catch(Exception ex) {
				log.error("Error while export : " , ex);
				throw new BusinessException(ex);
			}
		}
		return out;
	}

	private GetResDTO setContentVS(ValueSet valueset, FormatEnum format) {
		GetResDTO out = new GetResDTO();

		String resource = FHIRR4Helper.serializeResource(valueset, true, true, false);
		String oid = StringUtility.removeUrnOidFromSystem(valueset.getIdentifier().get(0).getValue());
		out.setOid(oid);
		out.setExportable(false);

		if(valueset.getMeta().getSecurity()==null || 
			valueset.getMeta().getSecurity().isEmpty() ||
			valueset.getMeta().getSecurity(SECURITY_SYSTEM, SECURITY_CODE_NORMAL)!=null) {
			try {
				out.setExportable(true);
				out.setContent(createByteContent(resource, oid, format));
			} catch(Exception ex) {
				log.error("Error while export : " , ex);
				throw new BusinessException(ex);
			}
		}
		return out;
	}

	private byte[] createByteContent(String resource, String oid, FormatEnum format) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		if(FormatEnum.FHIR_R4_JSON.equals(format)) {
			outputStream.write(resource.getBytes());
			return outputStream.toByteArray();
		} else {
			ConversionResponseDTO conversionResponseDTO = converter.callConvertFromFhirJson(format, oid,resource.getBytes());
			outputStream.write(conversionResponseDTO.getResult().getBytes());
			return outputStream.toByteArray();
		}
	}
	
}
