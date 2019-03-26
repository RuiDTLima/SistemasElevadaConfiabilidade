package pt.ist.sec.g27.hds_client.model;

import pt.ist.sec.g27.hds_client.SecurityUtils;
import pt.ist.sec.g27.hds_client.Utils;

import java.security.PrivateKey;

public class Message {
    private Body body;
    private byte[] signature;

    public Message() {
    }

    public Message(Body body, PrivateKey privateKey) throws Exception {
        this.body = body;
        this.signature = SecurityUtils.sign(privateKey, Utils.objectToByteArray(body));
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
