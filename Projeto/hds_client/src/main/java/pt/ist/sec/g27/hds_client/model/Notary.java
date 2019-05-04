package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Notary {
    private final static Logger log = LoggerFactory.getLogger(Notary.class);

    private int id;

    @JsonProperty("pubKey")
    private String pubKeyPath;
    private PublicKey publicKey;
    private String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String timestamp;

    public Notary() {
    }

    public int getId() {
        return id;
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

    public String getUrl() {
        return url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ZonedDateTime getTimestampInUTC() {
        return ZonedDateTime.parse(timestamp).withZoneSameInstant(ZoneOffset.UTC);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
