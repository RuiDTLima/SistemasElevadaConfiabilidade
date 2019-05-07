package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Good {
    private int id;

    @JsonProperty("owner-id")
    private int ownerId;
    private String name;
    private State state;

    @JsonProperty("wts")
    private int wTs;

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public int getwTs() {
        return wTs;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setwTs(int wTs) {
        this.wTs = wTs;
    }

    public void incrWts() {
        this.wTs++;
    }
}