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
import java.util.Scanner;

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

    public PrivateKey getPrivateKey() throws IOException {
        if (this.privateKey != null)
            return this.privateKey;
        if (this.privKeyPath == null) {
            String errorMessage = "The private key path is not defined in the state.";
            log.warn(errorMessage);
            System.out.println(errorMessage);
            throw new NullPointerException();
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String msg = "Please insert password: ";
            System.out.print(msg);
            System.out.flush();

            try {
                String password = scanner.next();
                this.privateKey = SecurityUtils.decryptPrivateKey(privKeyPath, password);
                break;
            } catch (IOException e) {
                msg = "File " + privKeyPath + " corrupted!";
                log.warn(msg);
                System.out.println(msg);
                throw e;
            } catch (Exception e) {
                msg = "Password incorrect! Try again.";
                log.warn(msg);
                System.out.println(msg);
            }
        }
        System.out.println("Password correct!");

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

    public boolean validateUser() {
        return privKeyPath != null;
    }
}
