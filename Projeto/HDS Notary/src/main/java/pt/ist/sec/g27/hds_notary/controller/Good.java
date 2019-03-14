package pt.ist.sec.g27.hds_notary.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.sec.g27.hds_notary.State;

public class Good {

    @JsonProperty("owner-id")
    private int ownerId;
    private String name;
    private State state;
}
