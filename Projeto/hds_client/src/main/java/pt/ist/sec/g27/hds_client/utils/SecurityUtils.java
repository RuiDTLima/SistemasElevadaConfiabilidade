package pt.ist.sec.g27.hds_client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtils {
    private final static Logger log = LoggerFactory.getLogger(SecurityUtils.class);
    private final static String ALGORITHM_FOR_VERIFY = "SHA256withRSA";

    public static PublicKey readPublic(String pubKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(pubKeyPath);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePublic(x509EncodedKeySpec);
    }

    public static PrivateKey readPrivate(String privKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(privKeyPath);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePrivate(pkcs8EncodedKeySpec);
    }

    private static byte[] read(String keyPath) throws IOException {
        ClassLoader classLoader = SecurityUtils.class.getClassLoader();
        URL resource = classLoader.getResource("keys/" + keyPath);
        try (FileInputStream inputStream = new FileInputStream(resource.getFile())) {
            byte[] encoded = new byte[inputStream.available()];
            inputStream.read(encoded);
            return encoded;
        } catch (IOException e) {
            log.warn("An error occurred while reading a file.", e);
            throw e;
        }
    }

    public static byte[] sign(PrivateKey privateKey, byte[] toSign) throws Exception {
        try {
            Signature signature = Signature.getInstance(ALGORITHM_FOR_VERIFY);
            signature.initSign(privateKey);
            signature.update(toSign);
            return signature.sign();
        } catch (Exception e) {
            log.warn("Something related to sign not worked properly.", e);
            throw e;// TODO change this to the correct exception
        }
    }

    public static boolean verify(PublicKey publicKey, byte[] notSigned, byte[] signed) throws Exception {
        try {
            Signature signature = Signature.getInstance(ALGORITHM_FOR_VERIFY);
            signature.initVerify(publicKey);
            signature.update(notSigned);
            return signature.verify(signed);
        } catch (Exception e) {
            log.warn("Something related to verify not worked properly.", e);
            throw e;// TODO change this to the correct exception
        }
    }
}
