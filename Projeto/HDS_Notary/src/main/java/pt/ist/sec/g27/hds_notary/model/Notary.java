package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.sec.g27.hds_notary.utils.SecurityUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class Notary {
    private int id;
    private String url;
    @JsonProperty("pubKey")
    private String pubKeyPath;
    private PublicKey publicKey;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("privKey")
    private String privateKeyPath;

    public Notary() {
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    @JsonIgnore
    public PublicKey getPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (this.publicKey != null)
            return this.publicKey;
        this.publicKey = SecurityUtils.readPublic(this.pubKeyPath);
        return this.publicKey;
    }
}
