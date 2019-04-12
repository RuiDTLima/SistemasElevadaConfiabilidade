package pt.ist.sec.g27.hds_client_malicious.exceptions;

public class ResponseException extends RuntimeException {
    public ResponseException(String message) {
        super(message);
    }
}