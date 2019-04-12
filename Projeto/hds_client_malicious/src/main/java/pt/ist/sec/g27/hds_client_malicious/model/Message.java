package pt.ist.sec.g27.hds_client_malicious.model;

public class Message {
    private Body body;
    private byte[] signature;

    public Message() {
    }

    public Message(Body body, byte[] signature) {
        this.body = body;
        this.signature = signature;
    }

    public Body getBody() {
        return body;
    }

    public byte[] getSignature() {
        return signature;
    }
}
