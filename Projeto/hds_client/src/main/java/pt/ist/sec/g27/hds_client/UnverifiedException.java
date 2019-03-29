package pt.ist.sec.g27.hds_client;

public class UnverifiedException extends RuntimeException {

    public UnverifiedException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
