package pt.ist.sec.g27.hds_client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtils {
    private final static Logger log = LoggerFactory.getLogger(SecurityUtils.class);
    private final static String ALGORITHM_FOR_VERIFY = "SHA256withRSA";
    private final static String SECRET_KEY_ALGORITHM = "AES";
    private final static String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private final static String PRIVATE_KEY_ALGORITHM = "RSA";
    private final static int KEY_LENGTH_IN_BYTES = 16; // Number of bytes for the key with "AES/ECB/PKCS5Padding"

    public static PublicKey readPublic(String pubKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(pubKeyPath);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePublic(x509EncodedKeySpec);
    }

    public static PrivateKey readPrivate(String privKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(privKeyPath);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);
        return rsa.generatePrivate(pkcs8EncodedKeySpec);
    }

    private static byte[] read(String keyPath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(keyPath)) {
            byte[] encoded = new byte[fileInputStream.available()];
            fileInputStream.read(encoded);
            return encoded;
        } catch (IOException e) {
            log.warn("An error occurred while reading a file.", e);
            throw e;
        }
    }

    public static byte[] sign(PrivateKey privateKey, byte[] toSign) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM_FOR_VERIFY);
            signature.initSign(privateKey);
            signature.update(toSign);
            return signature.sign();
        } catch (Exception e) {
            String errorMessage = "Something related to the signing of the message did not work properly.";
            log.warn(errorMessage);
            throw new UnverifiedException(errorMessage);
        }
    }

    public static boolean verify(PublicKey publicKey, byte[] notSigned, byte[] signed) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM_FOR_VERIFY);
            signature.initVerify(publicKey);
            signature.update(notSigned);
            return signature.verify(signed);
        } catch (Exception e) {
            String errorMessage = "Something related to the verification of the signature did not work properly.";
            log.warn(errorMessage);
            throw new UnverifiedException(errorMessage);
        }
    }

    public static PrivateKey decryptPrivateKey(String keyFile, String passPhrase) throws Exception {
        byte[] padded_key = getPaddedKey(passPhrase); // Get padded key
        SecretKey password = new SecretKeySpec(padded_key, SECRET_KEY_ALGORITHM); // Convert byte to key

        String base64EncryptedKey = FileUtils.fileToString(keyFile);
        byte[] encrypted_key_bytes = Base64.getMimeDecoder().decode(base64EncryptedKey);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        KeyFactory rsa = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);

        cipher.init(Cipher.DECRYPT_MODE, password); // Set to decryption mode
        byte[] decrypted_key = cipher.doFinal(encrypted_key_bytes);
        return rsa.generatePrivate(new PKCS8EncodedKeySpec(decrypted_key));
    }

    private static byte[] getPaddedKey(String passPhrase) throws Exception {
        byte[] key = passPhrase.getBytes(); // Convert string to byte array
        byte[] padded_key = new byte[KEY_LENGTH_IN_BYTES];
        int passPhraseLength = key.length;

        if (passPhraseLength > KEY_LENGTH_IN_BYTES)
            throw new Exception("Key must be smaller than " + KEY_LENGTH_IN_BYTES + " bytes!");

        // Pad the encryption key with 0's
        for (int i = 0; i < KEY_LENGTH_IN_BYTES; i++)
            padded_key[i] = i < passPhraseLength ? key[i] : 0;
        return padded_key;
    }
}
