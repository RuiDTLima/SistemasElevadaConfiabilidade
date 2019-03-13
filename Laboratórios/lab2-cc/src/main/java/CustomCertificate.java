import java.io.*;
import java.security.PublicKey;

public class CustomCertificate implements Serializable {

    private String algorithm;
    private PublicKey publicKey;
    private String name;

    public CustomCertificate(String algorithm, PublicKey publicKey, String name) {
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        this.name = name;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
