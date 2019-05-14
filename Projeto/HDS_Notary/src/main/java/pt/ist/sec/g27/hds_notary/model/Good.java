package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Good {
    private int id;

    @JsonProperty("owner-id")
    private int ownerId;
    @JsonProperty("signed-id")
    private int signedId;
    private String name;
    private State state;

    @JsonProperty("wts")
    private int wTs;

    private byte[] signature;

    public Good() {
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getSignedId() {
        return signedId;
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

    public byte[] getSignature() {
        return signature;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setSignedId(int signedId) {
        this.signedId = signedId;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setwTs(int wTs) {
        this.wTs = wTs;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}