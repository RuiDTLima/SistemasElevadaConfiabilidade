package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

    public TransferCertificate() {
    }

    public TransferCertificate(int buyerId, int sellerId, int goodId) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.goodId = goodId;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
    }
}
