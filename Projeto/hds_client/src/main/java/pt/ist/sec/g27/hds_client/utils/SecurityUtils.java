package pt.ist.sec.g27.hds_client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
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

    public static String encryptPrivateKey(String keyFile, String passPhrase)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            java.security.spec.InvalidKeySpecException,
            java.security.InvalidKeyException,
            java.io.IOException,
            javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException,
            Exception
    {
        byte[] padded_key = getPaddedKey(passPhrase); // Get padded key
        SecretKey password = new SecretKeySpec(padded_key, "AES"); // Convert byte to key

        byte[] encrypted_key;
        PrivateKey privateKey = readPrivate(keyFile); // Read file to private key
        byte[] private_key_bytes =  privateKey.getEncoded(); // Private key to bytes
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Aes with padding 128 bit = 16 byte
        String base64EncryptedKey;

        cipher.init(Cipher.ENCRYPT_MODE, password); // Set to encrypt mode
        encrypted_key = cipher.doFinal(private_key_bytes); // Private key encrypted
        base64EncryptedKey = Base64.getMimeEncoder().encodeToString(encrypted_key);

        return base64EncryptedKey;
    }

    public static PrivateKey decryptPrivateKey(String keyFile, String passPhrase)
            throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            java.security.spec.InvalidKeySpecException,
            java.security.InvalidKeyException,
            java.io.IOException,
            javax.crypto.IllegalBlockSizeException,
            javax.crypto.BadPaddingException,
            Exception
    {
        byte[] padded_key = getPaddedKey(passPhrase); // Get padded key
        SecretKey password = new SecretKeySpec(padded_key, "AES"); // Convert byte to key

        byte[] decrypted_key;
        String base64EncryptedKey = FileUtils.fileToString(keyFile);
        byte[] encrypted_key_bytes = Base64.getMimeDecoder().decode(base64EncryptedKey);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        PrivateKey privateKey;

        cipher.init(Cipher.DECRYPT_MODE, password); // Set to decryption mode
        decrypted_key = cipher.doFinal(encrypted_key_bytes);
        privateKey = rsa.generatePrivate(new PKCS8EncodedKeySpec(decrypted_key));

        return privateKey;
    }

    private static byte[] getPaddedKey(String passPhrase) throws Exception{
        int N = 16; // Number of bytes for the key with "AES/ECB/PKCS5Padding"
        byte[] key  = passPhrase.getBytes(); // Convert string to byte array
        byte[] padded_key = new byte[N];
        int passPhraseLength = key.length;

        if( key.length > N ){ throw new Exception("Key must be smaller than " + N + " bytes!"); }

        // Padd the encryption key with 0's
        for(int i=0 ; i<N ; i++){ padded_key[i] = i<passPhraseLength ? key[i] : 0; }
        return padded_key;
    }

}
