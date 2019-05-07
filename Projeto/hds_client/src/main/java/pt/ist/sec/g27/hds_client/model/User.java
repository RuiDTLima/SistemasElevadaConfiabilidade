package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class User {
    private final static Logger log = LoggerFactory.getLogger(User.class);

    private int id;
    private String name;

    @JsonProperty("pubKey")
    private String pubKeyPath;
    private PublicKey publicKey;

    @JsonProperty("privKey")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String privKeyPath;
    private PrivateKey privateKey;
    private String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String timestamp;

    public User() {
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
        if (this.pubKeyPath == null) {
            String errorMessage = "The public key path is not defined in the state.";
            log.warn(errorMessage);
            System.out.println(errorMessage);
            throw new NullPointerException();
        }
        this.publicKey = SecurityUtils.readPublic(this.pubKeyPath);
        return this.publicKey;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public String getUrl() {
        return url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ZonedDateTime getTimestampInUTC() {
        return ZonedDateTime.parse(timestamp).withZoneSameInstant(ZoneOffset.UTC);
    }

    public boolean validateUser(String password) {
        if (privKeyPath == null) {
            String errorMessage = "There is a problem with the state file. It needs a privKey for the user.";
            throw new IllegalStateException(errorMessage);
        }
        try {
            this.privateKey = SecurityUtils.decryptPrivateKey(privKeyPath, password);
        } catch (Exception e) {
            String errorMessage = "The password is incorrect. Try again.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return false;
        }
        return true;
    }
}
