package pt.ist.sec.g27.hds_notary.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    private ErrorModel error;

    public ErrorModel getError() {
        return error;
    }

    public void setError(ErrorModel error) {
        this.error = error;
    }

    public UnauthorizedException(ErrorModel error) {
        this.error = error;
    }
}
