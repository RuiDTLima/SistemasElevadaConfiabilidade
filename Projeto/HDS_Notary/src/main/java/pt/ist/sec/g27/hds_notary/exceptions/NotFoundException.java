package pt.ist.sec.g27.hds_notary.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends HttpExceptions {

    public NotFoundException(ErrorModel error) {
        super(error);
    }
}
