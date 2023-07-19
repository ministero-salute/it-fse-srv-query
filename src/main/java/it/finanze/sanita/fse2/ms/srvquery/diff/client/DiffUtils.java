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

import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffOpType.*;
import static it.finanze.sanita.fse2.ms.srvquery.diff.client.DiffResource.NO_VERSION;
import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;
import static org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb;

public final class DiffUtils {

    public static final int STANDARD_OFFSET = 2;

    public static Map<String, DiffResource> mapResourcesAsHistory(IGenericClient client, Bundle bundle, Date lastUpdate) {
        Map<String, DiffResource> resources = new HashMap<>();
        while (bundle != null) {
            // Add resources
            mapResources(bundle, resources, null, lastUpdate);
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

    public static Map<String, DiffResource> mapResourcesAs(IGenericClient client, Bundle bundle, HttpMethod method,  Date lastUpdate) {
        Map<String, DiffResource> resources = new HashMap<>();
        while (bundle != null) {
            // Add resources
            mapResources(bundle, resources, method, lastUpdate);
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

    private static void mapResources(Bundle bundle, Map<String, DiffResource> map, HttpMethod op, Date lastUpdate) {
        // Retrieve entries
        List<BundleEntryComponent> resources = bundle.getEntry();
        // Iterate
        for (BundleEntryComponent entry : resources) {
            // Get resource
            Resource res = entry.getResource();
            // Let me know the latest operation op
            HTTPVerb method = entry.getRequest().getMethod();
            // Obtain display value
            DiffOpType type;
            // Override method if provided
            if(op != null) {
                type = parseOpType(op.name());
            } else {
                type = parseOpType(method.getDisplay());
            }
            // Check if resource deleted
            if(res != null) {
                // Get id
                String id = asId(res);
                // Get version
                String version = asVersionId(res);
                // Register operation op
                map.putIfAbsent(id, new DiffResource(version, type));
                // If we reached the POST operation of a DELETED resource
                if(map.get(id).op() == DELETE && type == INSERT) {
                    // Retrieve creation date
                    Date insertionDate = res.getMeta().getLastUpdated();
                    // If defined and deleted in between the lastUpdate, throw it away
                    if(insertionDate.after(lastUpdate)) {
                        // Remove it
                        map.remove(id);
                    }
                }
                // If we reached the POST operation of an UPDATED resource
                else if(map.get(id).op() == UPDATE && type == INSERT) {
                    // Retrieve creation date
                    Date insertionDate = res.getMeta().getLastUpdated();
                    // If defined and updated in between the lastUpdate, define as INSERT instead of UPDATE
                    if(insertionDate.after(lastUpdate)) {
                        // Replace it
                        String root = map.get(id).version();
                        map.replace(id, new DiffResource(root, INSERT));
                    }
                }
            } else {
                // For deleted resource getResource() returns null,
                // so we return the id from getFullUrl
                map.putIfAbsent(asId(entry.getFullUrl()), new DiffResource(NO_VERSION, type));
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

    public static String asVersionId(IBaseResource res) {
        return res.getMeta().getVersionId();
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