package it.finanze.sanita.fse2.ms.srvquery.exceptions;


import lombok.Getter;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Logs.ERR_RES_NOT_FOUND;

@Getter
public class ResourceNotFoundException extends Exception {

    private final String resourceId;
    private final String versionId;

    public ResourceNotFoundException(String resourceId, String versionId) {
        super(ERR_RES_NOT_FOUND);
        this.resourceId = resourceId;
        this.versionId = versionId;
    }
}
