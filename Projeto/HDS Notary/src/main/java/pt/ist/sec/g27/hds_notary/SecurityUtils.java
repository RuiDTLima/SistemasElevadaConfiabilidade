package pt.ist.sec.g27.hds_notary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtils {

    private final static Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    public static PublicKey readPublic(String pubKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(pubKeyPath);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePublic(x509EncodedKeySpec);
    }

    private static byte[] read(String pubKeyPath) throws IOException {
        ClassLoader classLoader = SecurityUtils.class.getClassLoader();
        try (FileInputStream inputStream = new FileInputStream(classLoader.getResource(pubKeyPath).getFile())) {
            byte[] encoded = new byte[inputStream.available()];
            inputStream.read(encoded);
            return encoded;
        } catch (IOException e) {
            log.warn("An error occurred while reading a file.", e);
            throw e;
        }
    }
}
