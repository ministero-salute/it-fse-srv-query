package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;

public final class DiffUtils {

    public static final int STANDARD_OFFSET = 2;

    public static List<String> getResources(IGenericClient client, Bundle bundle) {
        List<String> resources = new ArrayList<>();
        while (bundle != null) {
            // Add resources
            resources.addAll(getResources(bundle));
            // Verify if next page is available
            if(bundle.getLink(LINK_NEXT) != null) {
                // Request next page
                bundle = client.loadPage().next(bundle).execute();
            } else {
                bundle = null;
            }
        }
        return resources;
    }

    private static List<String> getResources(Bundle bundle) {
        // Working var
        List<String> list = new ArrayList<>();
        // Retrieve entries
        List<Bundle.BundleEntryComponent> resources = bundle.getEntry();
        // Iterate
        for (Bundle.BundleEntryComponent entry : resources) {
            Resource res = entry.getResource();
            if(res != null) {
                list.add(asId(res));
            } else {
                // For deleted resource getResource() returns null,
                // so we return the id from getFullUrl
                list.add(asId(entry.getFullUrl()));
            }
        }
        // Return resources
        return list;
    }

    public static String asId(String uri) {
        int idx = uri.lastIndexOf("/") + 1;
        return uri.substring(idx);
    }

    public static String asId(IBaseResource res) {
        return res.getIdElement().getIdPart();
    }

    public static List<String> asId(List<Resource> res) {
        return res.stream().map(r -> r.getIdElement().getIdPart()).collect(Collectors.toList());
    }

    public static Date getCurrentTime() {
        return getTime(LocalDateTime.now(), STANDARD_OFFSET);
    }

    public static Date getCurrentTime(int offset) {
        return getTime(LocalDateTime.now(), offset);
    }

    public static Date getTime(LocalDateTime time, int offset) {
        return Date.from(time.toInstant(ZoneOffset.ofHours(offset)));
    }

    public static Date getTime(LocalDateTime time) {
        return Date.from(time.toInstant(ZoneOffset.ofHours(STANDARD_OFFSET)));
    }

}
