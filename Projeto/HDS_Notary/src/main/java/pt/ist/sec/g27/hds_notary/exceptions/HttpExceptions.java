package pt.ist.sec.g27.hds_notary.exceptions;

import pt.ist.sec.g27.hds_notary.model.ErrorModel;

public class HttpExceptions extends RuntimeException {
    private ErrorModel error;

    HttpExceptions(ErrorModel errorModel) {
        this.error = errorModel;
    }

    public ErrorModel getError() {
        return error;
    }

    public void setError(ErrorModel error) {
        this.error = error;
    }
}
