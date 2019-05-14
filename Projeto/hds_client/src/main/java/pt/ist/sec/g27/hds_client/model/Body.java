package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class Body implements Serializable {
    @JsonProperty("user-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int userId = -1;

    @JsonProperty("sender-id")
    private int senderId = -1;

    @JsonProperty("signed-id")
    private int signedId;

    @JsonProperty("good-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int goodId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String state;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String response;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HttpStatus status;

    @JsonProperty("transfer-certificate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TransferCertificate transferCertificate;

    @JsonProperty("wts")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int wTs = -1;

    @JsonProperty("rid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int rId = -1;

    private byte[] signature;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Message message;

    public Body() {
    }

    public Body(int senderId, int goodId, int id, boolean isRead) {
        if (isRead)
            this.rId = id;
        else
            this.wTs = id;
        this.senderId = senderId;
        this.goodId = goodId;
    }

    public Body(int senderId, int goodId, int id, boolean isRead, byte[] signature) {
        this(senderId, goodId, id, isRead);
        this.signature = signature;
    }

    public Body(int senderId, int goodId) {
        this.senderId = senderId;
        this.goodId = goodId;
    }

    public Body(int senderId, Message message) {
        this.senderId = senderId;
        this.status = message.getBody().getStatus();
        this.message = message;
    }

    public Body(int senderId, String response, Message message) {
        this.senderId = senderId;
        this.status = message.getBody().getStatus();
        this.message = message;
        this.response = response;
    }

    public Body(int senderId, int goodId, Message message, int wTs, byte[] signature) {
        this.senderId = senderId;
        this.goodId = goodId;
        this.message = message;
        this.wTs = wTs;
        this.signature = signature;
    }

    public Body(int senderId, RuntimeException runtimeException) {
        this.senderId = senderId;
        this.response = runtimeException.getMessage();
        this.status = HttpStatus.BAD_REQUEST;
    }

    public int getUserId() {
        return userId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getSignedId() {
        return signedId;
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

    public HttpStatus getStatus() {
        return status;
    }

    public TransferCertificate getTransferCertificate() {
        return transferCertificate;
    }

    public int getwTs() {
        return wTs;
    }

    public int getrId() {
        return rId;
    }

    public byte[] getSignature() {
        return signature;
    }

    public Message getMessage() {
        return message;
    }

    public void setrId(int rId) {
        this.rId = rId;
    }
}