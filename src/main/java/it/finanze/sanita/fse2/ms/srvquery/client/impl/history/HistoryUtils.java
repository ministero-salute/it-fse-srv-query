package it.finanze.sanita.fse2.ms.srvquery.client.impl.history;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ca.uhn.fhir.context.FhirVersionEnum.R4;
import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;

public class HistoryUtils {

    public static final int STANDARD_OFFSET = 2;

    public static void printJSON(Bundle bundle) {
        FhirContext context = FhirContext.forCached(R4);
        IParser parser = context.newJsonParser();
        parser.setPrettyPrint(true);
        System.out.println(parser.encodeResourceToString(bundle));
    }

    public static String asId(String uri) {
        int idx = uri.lastIndexOf("/") + 1;
        return uri.substring(idx);
    }

    public static String asId(IBaseResource res) {
        return res.getIdElement().getIdPart();
    }

    public static String asVersionId(IBaseResource res) {
        return res.getMeta().getVersionId();
    }

    public static List<String> asId(List<Resource> res) {
        return res.stream().map(r -> r.getIdElement().getIdPart()).collect(Collectors.toList());
    }

    public static Date getCurrentTime() {
        return getTime(LocalDateTime.now(), STANDARD_OFFSET);
    }

    public static Date getTime(LocalDateTime time, int offset) {
        return Date.from(time.toInstant(ZoneOffset.ofHours(offset)));
    }

    @Nullable
    public static Bundle hasNextPage(IGenericClient client, Bundle bundle) {
        // Verify if next page is available
        if(bundle.getLink(LINK_NEXT) != null) {
            // Request next page
            bundle = client.loadPage().next(bundle).execute();
        } else {
            bundle = null;
        }
        return bundle;
    }
}
