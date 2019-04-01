package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

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
    }

    public Body(int userId, int goodId) {
        this.userId = userId;
        this.goodId = goodId;
    }

    public Body(int userId, Message message) {
        this.userId = userId;
        this.message = message;
    }

    public Body(int userId, State state) {
        this.userId = userId;
        this.state = state.getState();
    }

    public Body(String response) {
        this.response = response;
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

    public Message getMessage() {
        return message;
    }
}