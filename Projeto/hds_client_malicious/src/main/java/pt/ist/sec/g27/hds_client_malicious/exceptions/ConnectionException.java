package pt.ist.sec.g27.hds_client_malicious.exceptions;

public class ConnectionException extends RuntimeException {
    public ConnectionException(String message) {
        super(message);
    }
}