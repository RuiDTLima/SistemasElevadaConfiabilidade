package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum State {

    @JsonProperty("not-on-sale")
    NOT_ON_SALE("not-on-sale"),
    SALE("sale"),
    VENDING("vending");

    private final String state;

    State(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
