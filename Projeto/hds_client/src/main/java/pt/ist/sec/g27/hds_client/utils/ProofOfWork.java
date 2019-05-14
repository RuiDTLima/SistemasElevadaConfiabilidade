package pt.ist.sec.g27.hds_client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Proof of work
public class ProofOfWork {
    private static final String SHA256 = "SHA-256";
    private static final int BYTE_SIZE = 8; // 8 BIT
    private static final int NUMBER_BITS = 20;

    public static BigInteger compute(Object body) throws NoSuchAlgorithmException, JsonProcessingException {
        MessageDigest digest = MessageDigest.getInstance(SHA256);
        byte[] message = Utils.jsonObjectToByteArray(body);

        BigInteger i = BigInteger.ZERO;

        while (true) {
            if (verify(digest, message, i))
                return i;
            i = i.add(BigInteger.ONE);
        }
    }

    private static boolean verify(MessageDigest digest, byte[] message, BigInteger i) {
        byte[] concatMessageI = concat(message, i.toByteArray());
        byte[] encodedHash = digest(digest, concatMessageI);
        int numberOfByte = (int) Math.ceil(NUMBER_BITS / 8.0);

        for (int j = 0; j < numberOfByte; j++) {
            for (int t = 0; t < BYTE_SIZE; t++) {
                int bit = (encodedHash[j] >> t) & 1;
                if (j * BYTE_SIZE + t >= NUMBER_BITS)
                    break;
                if (bit == 1)
                    return false;
            }
        }
        return true;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(a);
            outputStream.write(b);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] digest(MessageDigest digest, byte[] input) {
        byte[] hash = digest.digest(input);
        digest.reset();
        return hash;
    }
}
