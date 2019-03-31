package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class User {
    private int id;
    private String name;

    @JsonProperty("pubKey")
    private String pubKeyPath;
    private PublicKey publicKey; // TODO temos de ignorar no parse do json com anotacao???

    @JsonProperty("privKey")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String privKeyPath;
    private PrivateKey privateKey;
    private int port;

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

    public PublicKey getPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (this.publicKey != null)
            return this.publicKey;
        this.publicKey = SecurityUtils.readPublic(this.pubKeyPath);
        return this.publicKey;
    }

    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (this.privateKey != null)
            return this.privateKey;
        if (privKeyPath == null)
            return null;
        this.privateKey = SecurityUtils.readPrivate(privKeyPath);
        return this.privateKey;
    }

    public int getPort() {
        return port;
    }

    public boolean validateUser() {
        return privKeyPath != null;
    }
}
