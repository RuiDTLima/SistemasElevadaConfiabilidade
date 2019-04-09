package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

public class Me {
    private int id;
    private String name;

    @JsonProperty("privKey")
    private String privKeyPath;
    private PrivateKey privateKey;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (this.privateKey != null)
            return this.privateKey;
        this.privateKey = SecurityUtils.readPrivate(this.privKeyPath);
        return this.privateKey;
    }
}
