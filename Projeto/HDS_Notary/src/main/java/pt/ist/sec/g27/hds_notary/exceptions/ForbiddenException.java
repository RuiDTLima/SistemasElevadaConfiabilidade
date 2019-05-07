package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends HttpExceptions {
    public ForbiddenException(String errorMessage, int rId, int wTs) {
        super(errorMessage, HttpStatus.FORBIDDEN, rId, wTs);
    }
}
