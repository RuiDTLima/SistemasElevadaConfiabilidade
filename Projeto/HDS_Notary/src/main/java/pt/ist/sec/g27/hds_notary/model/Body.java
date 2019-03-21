package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Body {
    @JsonProperty("owner-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int ownerId;

    @JsonProperty("buyer-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int buyerId;

    @JsonProperty("good-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int goodId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String state;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String response;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Message message;

    public Body() {
    }

    public Body(int buyerId, int goodId) {
        this.buyerId = buyerId;
        this.goodId = goodId;
    }

    public Body(int ownerId, Message message) {
        this.ownerId = ownerId;
        this.message = message;
    }

    public Body(int ownerId, State state) {
        this.ownerId = ownerId;
        this.state = state.getState();
    }

    public Body(String response) {
        this.response = response;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public int getGoodId() {
        return goodId;
    }

    public String getState() {
        return state;
    }

    public String getResponse() {
        return response;
    }

    public Message getMessage() {
        return message;
    }
}