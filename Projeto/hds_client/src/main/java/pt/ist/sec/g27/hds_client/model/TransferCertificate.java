package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferCertificate {
    @JsonProperty("buyer-id")
    private int buyerId;

    @JsonProperty("seller-id")
    private int sellerId;

    @JsonProperty("good-id")
    private int goodId;

    private String timestamp;

    public int getBuyerId() {
        return buyerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public int getGoodId() {
        return goodId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
