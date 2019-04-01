package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import pt.ist.sec.g27.hds_notary.exceptions.HttpExceptions;

import java.io.Serializable;

public class Body implements Serializable {
    @JsonProperty("user-id")
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Message message;

    public Body() {
        this.status = HttpStatus.OK;
    }

    public Body(int userId, int goodId) {
        this();
        this.userId = userId;
        this.goodId = goodId;
    }

    public Body(int userId, Message message) {
        this();
        this.userId = userId;
        this.message = message;
    }

    public Body(int userId, State state) {
        this();
        this.userId = userId;
        this.state = state.getState();
    }

    public Body(String response) {
        this();
        this.response = response;
    }

    public Body(HttpExceptions httpExceptions) {
        this.response = httpExceptions.getError().getErrorMessage();
        this.status = httpExceptions.getHttpStatus();
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

    public Message getMessage() {
        return message;
    }
}