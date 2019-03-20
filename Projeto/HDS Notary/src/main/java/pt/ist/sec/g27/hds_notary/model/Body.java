package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Body {

    @JsonProperty("user-id")
    private int userId;
    @JsonProperty("good-id")
    private int goodId;

    public int getUserId() {
        return userId;
    }

    public int getGoodId() {
        return goodId;
    }
}
