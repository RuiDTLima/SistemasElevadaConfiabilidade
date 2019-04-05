package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import pt.ist.sec.g27.hds_notary.exceptions.HttpExceptions;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Body implements Serializable {
    @JsonProperty(value = "user-id", defaultValue = "-1")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int userId;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Message message;

    public Body() {
    }

    public Body(int userId, int goodId) {
        this.status = HttpStatus.OK;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.userId = userId;
        this.goodId = goodId;
    }

    public Body(int userId, Message message) {
        this.status = HttpStatus.OK;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.userId = userId;
        this.message = message;
    }

    public Body(int userId, State state) {
        this.status = HttpStatus.OK;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.userId = userId;
        this.state = state.getState();
    }

    public Body(String response) {
        this.status = HttpStatus.OK;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.response = response;
    }

    public Body(String response, TransferCertificate transferCertificate) {
        this.status = HttpStatus.OK;
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
        this.response = response;
        this.transferCertificate = transferCertificate;
    }

    public Body(HttpExceptions httpExceptions) {
        this.response = httpExceptions.getErrorMessage();
        this.status = httpExceptions.getHttpStatus();
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
    }

    public int getUserId() {
        return userId;
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

    public String getTimestamp() {
        return timestamp;
    }

    @JsonIgnore
    public ZonedDateTime getTimestampInUTC() {
        return ZonedDateTime.parse(timestamp).withZoneSameInstant(ZoneOffset.UTC);
    }

    public Message getMessage() {
        return message;
    }
}