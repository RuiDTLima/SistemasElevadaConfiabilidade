package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;

public class HttpExceptions extends RuntimeException {
    private ErrorModel error;
    private HttpStatus httpStatus;

    HttpExceptions(ErrorModel errorModel, HttpStatus httpStatus) {
        this.error = errorModel;
        this.httpStatus = httpStatus;
    }

    public ErrorModel getError() {
        return error;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
