package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpExceptions {
    public UnauthorizedException(String errorMessage, int rId, int wTs) {
        super(errorMessage, HttpStatus.UNAUTHORIZED, rId, wTs);
    }
}
