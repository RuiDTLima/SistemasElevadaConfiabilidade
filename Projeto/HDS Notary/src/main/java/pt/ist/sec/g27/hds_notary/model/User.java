package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.sec.g27.hds_notary.SecurityUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class User {

    private int id;
    private String name;

    @JsonProperty("pubKey")
    private String pubKeyPath;
    private PublicKey publicKey; // TODO temos de ignorar no parse do json com anotacao???

    public User() {
    }

    public User(int id, String name, String pubKeyPath) {
        this.id = id;
        this.name = name;
        this.pubKeyPath = pubKeyPath;
        this.publicKey = null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPubKeyPath() {
        return pubKeyPath;
    }

    public PublicKey getPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (this.publicKey != null)
            return this.publicKey;
        this.publicKey = SecurityUtils.readPublic(this.pubKeyPath);
        return this.publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
