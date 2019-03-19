package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum State {
    @JsonProperty("not-on-sale")
    NOT_ON_SALE("not-on-sale"),

    @JsonProperty("on-sale")
    ON_SALE("on-sale"),
    VENDING("vending");

    private final String state;

    State(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}