package it.finanze.sanita.fse2.ms.srvquery.enums.history;

import org.springframework.http.HttpMethod;

public enum HistoryOperationEnum {
    INSERT,
    UPDATE,
    DELETE;

    public static HistoryOperationEnum parseHistoryOp(String method) {
        HttpMethod m = HttpMethod.resolve(method);
        if(m == null) {
            throw new IllegalArgumentException("Unknown operation op: " + method);
        }
        HistoryOperationEnum op;
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
                throw new IllegalArgumentException("Unsupported operation op: " + m.name());
        }
        return op;
    }
}
