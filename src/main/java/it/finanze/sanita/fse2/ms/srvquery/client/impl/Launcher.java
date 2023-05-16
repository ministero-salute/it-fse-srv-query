//package it.finanze.sanita.fse2.ms.srvquery.client.impl;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
//import org.hl7.fhir.r4.model.CodeSystem;
//import org.hl7.fhir.r4.model.ConceptMap;
//import org.hl7.fhir.r4.model.MetadataResource;
//import org.hl7.fhir.r4.model.ValueSet;
//
//import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
//import it.finanze.sanita.fse2.ms.srvquery.dto.ValidateCodeResultDTO;
//
//public class Launcher {
//
//	public static void main(String[] args) {
//		 FHIRClient fc = new FHIRClient("http://hapi.fhir.org/baseR4");
////		FHIRClient fc = new FHIRClient("http://localhost:8080/fhir/");
//		 
//		List<CodeDTO> codesCS1 = new ArrayList<>();
//		codesCS1.add(new CodeDTO("G", "Gold", null));
//		codesCS1.add(new CodeDTO("S", "Silver", null));
//		codesCS1.add(new CodeDTO("B", "Bronze", null));
//
//		String codeSystemMedalID = fc.insertCS("cs-medal", codesCS1);
//		CodeSystem codeSystemMedal = fc.readCS(codeSystemMedalID);
//
//		System.out.println("01) CREATE CS " + codeSystemMedal.getName() + " ID[" + codeSystemMedalID + "]");
//
//		List<CodeDTO> codesCS2 = new ArrayList<>();
//		codesCS2.add(new CodeDTO("I", "First", null));
//		codesCS2.add(new CodeDTO("II", "Second", null));
//		codesCS2.add(new CodeDTO("III", "Third", null));
//		codesCS2.add(new CodeDTO("IV", "Fouth", null));
//
//		String codeSystemPositionID = fc.insertCS("cs-position", codesCS2);
//		CodeSystem codeSystemPosition = fc.readCS(codeSystemPositionID);
//
//		System.out.println("02) CREATE CS " + codeSystemPosition.getName() + " ID[" + codeSystemPositionID + "]");
//
//		Map<MetadataResource, List<CodeDTO>> codesMedalVS = new HashMap<>();
//		codesMedalVS.put(codeSystemMedal, codesCS1);
//		String valueSetMedalID = fc.insertVS("vs-medal", null, codesMedalVS);
//		ValueSet valueSetMedal = fc.readVS(valueSetMedalID);
//
//		System.out.println("03) CREATE VS " + valueSetMedal.getName() + " DA " + codeSystemPosition.getName() + " ID[" + valueSetMedalID + "]");
//
//		String csSystem = codeSystemMedal.getId().split("/_history")[0];
//		ValidateCodeResultDTO vcr = fc.validateMetadataResource(csSystem, "G", valueSetMedal);
//		System.out.println("04) VALIDATE G IN " + valueSetMedal.getName() + " OUT: " + vcr.getResult());
//
//		List<CodeDTO> codesCSPosition = new ArrayList<>();
//		codesCS2.add(new CodeDTO("I", "First", null));
//		codesCS2.add(new CodeDTO("II", "Second", null));
//		codesCS2.add(new CodeDTO("III", "Third", null));
//
//		Map<MetadataResource, List<CodeDTO>> codesPositionVS = new HashMap<>();
//		codesPositionVS.put(codeSystemMedal, codesCSPosition);
//		String valueSetPositionID = fc.insertVS("vs-position", null, codesPositionVS);
//		ValueSet valueSetPosition = fc.readVS(valueSetPositionID);
//
//		System.out.println("05) CREATE VS " + valueSetPosition.getName() + " ID[" + valueSetPositionID + "]");
//
//
//		List<CodeSystem> css = fc.searchActiveCodeSystem();
//		System.out.println("06) CSS MEDAL IS ACTIVE? " + checkCSS(css, codeSystemMedalID));
//		System.out.println("07) CSS POSITION IS ACTIVE? " + checkCSS(css, codeSystemPositionID));
//
//		List<ValueSet> vss = fc.searchActiveValueSet();
//		System.out.println("08) VSS MEDAL IS ACTIVE? " + checkVSS(vss, valueSetMedalID));
//		System.out.println("09) VSS POSITION IS ACTIVE? " + checkVSS(vss, valueSetPositionID));
//
//		Calendar calendar = Calendar.getInstance();
//		calendar.add(Calendar.DATE, -1);
//		Date yesterday = calendar.getTime();
//		List<CodeSystem> cssYesterday = fc.searchModifiedCodeSystem(yesterday);
//		System.out.println("10) CSS MEDAL MODIFIED SINCE YESTERDAY? " + checkCSS(cssYesterday, codeSystemMedalID));
//		System.out.println("11) CSS POSITION MODIFIED SINCE YESTERDAY? " + checkCSS(cssYesterday, codeSystemPositionID));
//
//		List<ValueSet> vssYesterday = fc.searchModifiedValueSet(yesterday);
//		System.out.println("12) VSS MEDAL MODIFIED SINCE YESTERDAY? " + checkVSS(vssYesterday, valueSetMedalID));
//		System.out.println("13) VSS POSITION MODIFIED SINCE YESTERDAY? " + checkVSS(vssYesterday, valueSetPositionID));
//
//		Map<String, String> src2trgCodes = new HashMap<>();
//		src2trgCodes.put("G", "I");
//		src2trgCodes.put("S", "II");
//		src2trgCodes.put("B", "III");
//		String cmID = fc.insertCM("cm-medal-position", null, valueSetMedal, valueSetPosition, src2trgCodes );
//		ConceptMap cmMedalPosition = fc.readCM(cmID);
//
//		System.out.println("14) CREATE CM " + cmMedalPosition.getName() + " ID[" + cmID + "]");
//
//		System.out.println("15) FOUNDED CM " + fc.searchConceptMapBySourceSystem(valueSetMedal).iterator().next().getId());
//
//		String vsSystem = valueSetMedal.getId().split("/_history")[0];
//		CodeDTO code = fc.translate(vsSystem, "G", valueSetPosition);
//		System.out.println("16) TRANSLATE G -> CODE: " + code.getCode() + " - SYSTEM: " + code.getSystem());
//	}
//
//	private static Boolean checkCSS(List<CodeSystem> css, String id) {
//		Boolean out = false;
//		for (CodeSystem cs:css) {
//			if (cs.getId().contains(id)) {
//				out = true;
//				break;
//			}
//		}
//		return out;
//	}
//
//	private static Boolean checkVSS(List<ValueSet> vss, String id) {
//		Boolean out = false;
//		for (ValueSet vs:vss) {
//			if (vs.getId().contains(id)) {
//				out = true;
//				break;
//			}
//		}
//		return out;
//	}
//
//}