package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;

public class HttpExceptions extends RuntimeException {
    private final String errorMessage;
    private final HttpStatus httpStatus;
    private final int rId;
    private final int wTs;

    HttpExceptions(String errorMessage, HttpStatus httpStatus, int rId, int wTs) {
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
        this.rId = rId;
        this.wTs = wTs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getrId() {
        return rId;
    }

    public int getwTs() {
        return wTs;
    }
}
