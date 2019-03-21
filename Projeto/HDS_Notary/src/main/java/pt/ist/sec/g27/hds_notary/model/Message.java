package pt.ist.sec.g27.hds_notary.model;

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
