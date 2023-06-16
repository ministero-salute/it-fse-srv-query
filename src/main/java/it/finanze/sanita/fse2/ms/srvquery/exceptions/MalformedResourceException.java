package it.finanze.sanita.fse2.ms.srvquery.exceptions;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MalformedResourceException extends Exception {
    private final String resourceId;
    private final String versionId;
    private final String message;
}
