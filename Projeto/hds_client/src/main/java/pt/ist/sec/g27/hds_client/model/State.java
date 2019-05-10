package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum State {
    @JsonProperty("not-on-sale")
    NOT_ON_SALE("not-on-sale"),

    @JsonProperty("on-sale")
    ON_SALE("on-sale");

    private final String state;

    State(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static State getStateFromString(String state) {
        switch (state) {
            case "not-on-sale":
                return NOT_ON_SALE;
            case "on-sale":
                return ON_SALE;
            default:
                throw new IllegalArgumentException("The received state is invalid.");
        }
    }
}