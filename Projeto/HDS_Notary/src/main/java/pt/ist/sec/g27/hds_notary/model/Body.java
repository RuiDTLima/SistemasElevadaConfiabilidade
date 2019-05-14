package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import pt.ist.sec.g27.hds_notary.exceptions.HttpExceptions;

import java.io.Serializable;

public class Body implements Serializable {
    @JsonProperty("user-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int userId = -1;

    @JsonProperty("sender-id")
    private int senderId;

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
    private int wTs;

    @JsonProperty("rid")
    private int rId;

    private byte[] signature;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Message message;

    public Body() {
    }

    public Body(int senderId, int userId, State state, int rId, int wTs, byte[] signature, int signedId) {
        this.senderId = senderId;
        this.status = HttpStatus.OK;
        this.userId = userId;
        this.state = state.getState();
        this.rId = rId;
        this.wTs = wTs;
        this.signature = signature;
        this.signedId = signedId;
    }

    public Body(int senderId, String response, int rId, int wTs) {
        this.senderId = senderId;
        this.status = HttpStatus.OK;
        this.response = response;
        this.rId = rId;
        this.wTs = wTs;
    }

    public Body(int senderId, String response, TransferCertificate transferCertificate, int wTs) {
        this.senderId = senderId;
        this.status = HttpStatus.OK;
        this.response = response;
        this.transferCertificate = transferCertificate;
        this.rId = -1;
        this.wTs = wTs;
    }

    public Body(int senderId, HttpExceptions httpExceptions) {
        this.senderId = senderId;
        this.response = httpExceptions.getErrorMessage();
        this.status = httpExceptions.getHttpStatus();
        this.rId = httpExceptions.getrId();
        this.wTs = httpExceptions.getwTs();
    }

    public Body(int senderId, Message message) {
        this.senderId = senderId;
        this.message = message;
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
}