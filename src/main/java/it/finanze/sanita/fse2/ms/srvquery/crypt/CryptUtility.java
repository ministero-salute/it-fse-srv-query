package it.finanze.sanita.fse2.ms.srvquery.crypt;


import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CryptUtility {
 
	private static final Environment env = new StandardEnvironment();


	public static void encryptPatientInfo(Bundle bundle) {
		if (bundle == null || bundle.getEntry() == null) {
			return;
		}

		String originalPatientUrl = null;
		String newPatientFullUrl = null;

		for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
			Resource resource = entry.getResource();
			if (resource instanceof Patient) {
				Patient patient = (Patient) resource;

				Identifier identifier = patient.getIdentifier().get(0);
				String fiscalCode = identifier.getValue();

				Bundle.BundleEntryRequestComponent request = entry.getRequest();
				if (request != null && request.hasUrl()
						&& request.getUrl().contains("identifier=")) {
					String sha256 =
							StringUtility.encodeSHA256(fiscalCode.getBytes(StandardCharsets.UTF_8));
					patient.setId(sha256);
					originalPatientUrl = entry.getFullUrl();
					newPatientFullUrl = "https://example/Patient/" + sha256;
					entry.setFullUrl(newPatientFullUrl);
					String newUrl = "Patient/" + sha256;
					request.setUrl(newUrl);
					break;
				}
				break;
			}
		}

		// Aggiorna tutti i riferimenti al Patient nel Bundle
		if (originalPatientUrl != null && newPatientFullUrl != null) {
			for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
				Resource resource = entry.getResource();
				List<Reference> references = getAllReferences(resource);
				for (Reference ref : references) {
					if (ref.getReference() != null
							&& ref.getReference().equals(originalPatientUrl)) {
						ref.setReference(newPatientFullUrl);
					}
				}
			}
		}
	}


	public static List<Reference> getAllReferences(Resource resource) {
		List<Reference> references = new ArrayList<>();

		// Scorri tutti i metodi getXyz che ritornano un Reference
		for (Method method : resource.getClass().getMethods()) {
			if (Reference.class.equals(method.getReturnType()) && method.getParameterCount() == 0
					&& method.getName().startsWith("get")) {
				try {
					Reference ref = (Reference) method.invoke(resource);
					if (ref != null) {
						references.add(ref);
					}
				} catch (Exception ignored) {
				}
			}

			// Gestione dei campi List<Reference>
			if (List.class.equals(method.getReturnType())
					&& method.getGenericReturnType().getTypeName().contains("Reference")) {
				try {
					List<?> result = (List<?>) method.invoke(resource);
					if (result != null) {
						for (Object item : result) {
							if (item instanceof Reference) {
								references.add((Reference) item);
							}
						}
					}
				} catch (Exception ignored) {
				}
			}
		}

		return references;
	}


	private static boolean isDevProfile() {
		for (String profile : env.getActiveProfiles()) {
			if ("dev".equalsIgnoreCase(profile)) {
				return true;
			}
		}
		return false;
	}
 

}