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
    private int senderId;

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

    public Body(int senderId, int goodId) {
        this.senderId = senderId;
        this.goodId = goodId;
    }

    /*public Body(int senderId, Message message) {
        this.senderId = senderId;
        this.status = message.getBody().getStatus();
        this.message = message;
    }*/

    public Body(int senderId, int goodId, Message message, int wTs) {
        this.senderId = senderId;
        this.goodId = goodId;
        this.message = message;
        this.wTs = wTs;
    }

    /*public Body(RuntimeException runtimeException) {
        this.response = runtimeException.getMessage();
        this.status = HttpStatus.BAD_REQUEST;
    }*/

    public int getUserId() {
        return userId;
    }

    public int getSenderId() {
        return senderId;
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

    public Message getMessage() {
        return message;
    }
}