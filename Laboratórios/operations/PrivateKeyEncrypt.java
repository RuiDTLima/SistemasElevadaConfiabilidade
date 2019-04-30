

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Stream;


public class PrivateKeyEncrypt {

    // Works with base64
    private static String fileToString(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8);
        stream.forEach(s -> contentBuilder.append(s).append("\n"));
        return contentBuilder.toString();
    }

    public static PrivateKey readPrivate(String privKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(privKeyPath);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePrivate(pkcs8EncodedKeySpec);
    }

    private static byte[] read(String keyPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(keyPath);
        byte[] encoded = new byte[fileInputStream.available()];
        fileInputStream.read(encoded);
        return encoded;
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

    public static String encryptPrivateKey(String filename, String passPhrase)
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
        PrivateKey privateKey = readPrivate(filename); // Read file to private key
        byte[] private_key_bytes =  privateKey.getEncoded(); // Private key to bytes
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Aes with padding 128 bit = 16 byte
        String base64EncryptedKey;

        cipher.init(Cipher.ENCRYPT_MODE, password); // Set to encrypt mode
        encrypted_key = cipher.doFinal(private_key_bytes); // Private key encrypted
        base64EncryptedKey = Base64.getMimeEncoder().encodeToString(encrypted_key);

        return base64EncryptedKey;
    }

    public static PrivateKey decryptPrivateKey(String filename, String passPhrase)
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
        String base64EncryptedKey = fileToString(filename);
        byte[] encrypted_key_bytes = Base64.getMimeDecoder().decode(base64EncryptedKey);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        PrivateKey privateKey;

        cipher.init(Cipher.DECRYPT_MODE, password); // Set to decryption mode
        decrypted_key = cipher.doFinal(encrypted_key_bytes);
        privateKey = rsa.generatePrivate(new PKCS8EncodedKeySpec(decrypted_key));

        return privateKey;
    }

    public static void main(String[] args) throws Exception{

        if(args.length < 2){
            System.out.println("Use: <private-key file> <passPhrase>");
            System.exit(1);
        }

        System.out.print(encryptPrivateKey(args[0], args[1]));
    }

}
