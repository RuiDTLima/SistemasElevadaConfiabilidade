package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;

public class UnauthorizedException extends HttpExceptions {

    public UnauthorizedException(ErrorModel error) {
        super(error, HttpStatus.UNAUTHORIZED);
    }
}
