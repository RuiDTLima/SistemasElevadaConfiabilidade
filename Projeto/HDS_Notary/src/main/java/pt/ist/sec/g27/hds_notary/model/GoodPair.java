package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoodPair {

    @JsonProperty("owner-id")
    private int ownerId;
    private String state;

    public GoodPair(int ownerId, State state) {
        this.ownerId = ownerId;
        this.state = state.getState();
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getState() {
        return state;
    }
}
