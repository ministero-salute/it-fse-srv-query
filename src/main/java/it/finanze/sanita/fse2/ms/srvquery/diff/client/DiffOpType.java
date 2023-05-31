package it.finanze.sanita.fse2.ms.srvquery.diff.client;

import org.springframework.http.HttpMethod;

public enum DiffOpType {
    INSERT,
    UPDATE,
    DELETE;

    public static DiffOpType parseOpType(String method) {
        HttpMethod m = HttpMethod.resolve(method);
        if(m == null) {
            throw new IllegalArgumentException("Unknown operation type: " + method);
        }
        DiffOpType op;
        switch (m) {
            case POST:
                op = INSERT;
                break;
            case PUT:
            case PATCH:
                op = UPDATE;
                break;
            case DELETE:
                op = DELETE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation type: " + m.name());
        }
        return op;
    }
}
