package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;

public class HttpExceptions extends RuntimeException {
    private String errorMessage;
    private HttpStatus httpStatus;

    HttpExceptions(String errorMessage, HttpStatus httpStatus) {
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
