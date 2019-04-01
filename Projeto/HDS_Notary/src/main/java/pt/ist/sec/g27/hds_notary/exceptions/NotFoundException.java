package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;

public class NotFoundException extends HttpExceptions {

    public NotFoundException(ErrorModel error) {
        super(error, HttpStatus.NOT_FOUND);
    }
}
