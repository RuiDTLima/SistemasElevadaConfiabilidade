package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends HttpExceptions {
    public NotFoundException(String errorMessage, int rId, int wTs) {
        super(errorMessage, HttpStatus.NOT_FOUND, rId, wTs);
    }
}
