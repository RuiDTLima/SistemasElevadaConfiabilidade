package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;

public class Message {
    private Body body;
    private byte[] signature;

    @JsonProperty("proof-of-work")
    private BigInteger proofOfWork;

    public Message() {
    }

    public Message(Body body, byte[] signature) {
        this.body = body;
        this.signature = signature;
    }

    public Message(Body body, byte[] signature, BigInteger proofOfWork) {
        this.body = body;
        this.signature = signature;
        this.proofOfWork = proofOfWork;
    }

    public Body getBody() {
        return body;
    }

    public byte[] getSignature() {
        return signature;
    }

    public BigInteger getProofOfWork() {
        return proofOfWork;
    }
}
