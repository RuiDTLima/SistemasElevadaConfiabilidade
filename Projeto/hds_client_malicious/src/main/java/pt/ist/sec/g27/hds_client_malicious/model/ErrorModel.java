package pt.ist.sec.g27.hds_client_malicious.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorModel {
    @JsonProperty("error-message")
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorModel(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
