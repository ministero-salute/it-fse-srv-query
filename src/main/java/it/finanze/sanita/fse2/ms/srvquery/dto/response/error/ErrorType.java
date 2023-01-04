/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.dto.response.error;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    SERVER("/err/server", "Server error");

    private final String type;
    private final String title;

    public String toInstance(String instance) {
        return UriComponentsBuilder
            .fromUriString(instance)
            .build()
            .toUriString();
    }

    public String toInstance(String instance, String ...members) {
        return UriComponentsBuilder
            .fromUriString(instance)
            .pathSegment(members)
            .build()
            .toUriString();
    }


}
