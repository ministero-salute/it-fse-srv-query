package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class DiffUtils {

    public static final int STANDARD_OFFSET = 2;

    public static List<Resource> getResources(Bundle bundle) {
        // Retrieve entries
        List<Bundle.BundleEntryComponent> resources = bundle.getEntry();
        // Return resources
        return resources.stream().map(Bundle.BundleEntryComponent::getResource).collect(Collectors.toList());
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
