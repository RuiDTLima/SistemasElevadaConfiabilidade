import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Stream;

public class PrivateKeyEncrypt {
    private final static String SECRET_KEY_ALGORITHM = "AES";
    private final static String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private final static String PRIVATE_KEY_ALGORITHM = "RSA";
    private final static int KEY_LENGTH_IN_BYTES = 16; // Number of bytes for the key with "AES/ECB/PKCS5Padding"

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Use: <private-key file> <passPhrase>");
            return;
        }
        final String filename = args[0];

        Files.write(Paths.get(filename + ".enc"), encryptPrivateKey(filename, args[1]).getBytes(), StandardOpenOption.CREATE);
        System.out.println("Done!");
    }

    // Works with base64
    private static String fileToString(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);
        stream.forEach(s -> contentBuilder.append(s).append("\n"));
        return contentBuilder.toString();
    }

    public static PrivateKey readPrivate(String privKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(privKeyPath);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);
        return rsa.generatePrivate(pkcs8EncodedKeySpec);
    }

    private static byte[] read(String keyPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(keyPath);
        byte[] encoded = new byte[fileInputStream.available()];
        fileInputStream.read(encoded);
        return encoded;
    }

    private static byte[] getPaddedKey(String passPhrase) throws Exception {
        byte[] key = passPhrase.getBytes(); // Convert string to byte array
        byte[] padded_key = new byte[KEY_LENGTH_IN_BYTES];
        int passPhraseLength = key.length;

        if (passPhraseLength > KEY_LENGTH_IN_BYTES)
            throw new Exception("Key must be smaller than " + KEY_LENGTH_IN_BYTES + " bytes!");

        // Padd the encryption key with 0's
        for (int i = 0; i < KEY_LENGTH_IN_BYTES; i++)
            padded_key[i] = i < passPhraseLength ? key[i] : 0;
        return padded_key;
    }

    public static String encryptPrivateKey(String filename, String passPhrase) throws Exception {
        byte[] padded_key = getPaddedKey(passPhrase); // Get padded key
        SecretKey password = new SecretKeySpec(padded_key, SECRET_KEY_ALGORITHM); // Convert byte to key

        PrivateKey privateKey = readPrivate(filename); // Read file to private key
        byte[] private_key_bytes = privateKey.getEncoded(); // Private key to bytes
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM); // Aes with padding 128 bit = 16 byte

        cipher.init(Cipher.ENCRYPT_MODE, password); // Set to encrypt mode
        byte[] encrypted_key = cipher.doFinal(private_key_bytes); // Private key encrypted
        return Base64.getMimeEncoder().encodeToString(encrypted_key);
    }

    public static PrivateKey decryptPrivateKey(String filename, String passPhrase) throws Exception {
        byte[] padded_key = getPaddedKey(passPhrase); // Get padded key
        SecretKey password = new SecretKeySpec(padded_key, SECRET_KEY_ALGORITHM); // Convert byte to key

        String base64EncryptedKey = fileToString(filename);
        byte[] encrypted_key_bytes = Base64.getMimeDecoder().decode(base64EncryptedKey);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        KeyFactory rsa = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);
        PrivateKey privateKey;

        cipher.init(Cipher.DECRYPT_MODE, password); // Set to decryption mode
        byte[] decrypted_key = cipher.doFinal(encrypted_key_bytes);
        return rsa.generatePrivate(new PKCS8EncodedKeySpec(decrypted_key));
    }
}
