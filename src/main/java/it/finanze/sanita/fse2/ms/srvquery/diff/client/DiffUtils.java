package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;
import static org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb;

public final class DiffUtils {

    public static final int STANDARD_OFFSET = 2;

    public static Map<String, DiffOpType> mapResourcesAsHistory(IGenericClient client, Bundle bundle) {
        Map<String, DiffOpType> resources = new HashMap<>();
        while (bundle != null) {
            // Add resources
            mapResources(bundle, resources, null);
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

    public static Map<String, DiffOpType> mapResourcesAs(IGenericClient client, Bundle bundle, HttpMethod method) {
        Map<String, DiffOpType> resources = new HashMap<>();
        while (bundle != null) {
            // Add resources
            mapResources(bundle, resources, method);
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

    private static void mapResources(Bundle bundle, Map<String, DiffOpType> map, HttpMethod op) {
        // Retrieve entries
        List<BundleEntryComponent> resources = bundle.getEntry();
        // Iterate
        for (BundleEntryComponent entry : resources) {
            // Get resource
            Resource res = entry.getResource();
            // Let me know the latest operation type
            HTTPVerb method = entry.getRequest().getMethod();
            // Obtain display value
            String type;
            // Override method if provided
            if(op != null) {
                type = op.name();
            } else {
                type = method.getDisplay();
            }
            // Check if resource deleted
            if(res != null) {
                map.putIfAbsent(asId(res), DiffOpType.parse(type));
            } else {
                // For deleted resource getResource() returns null,
                // so we return the id from getFullUrl
                map.putIfAbsent(asId(entry.getFullUrl()), DiffOpType.parse(type));
            }
        }
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

}
