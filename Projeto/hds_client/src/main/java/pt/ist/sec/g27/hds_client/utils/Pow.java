package pt.ist.sec.g27.hds_client.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Proof of work
public class Pow {
    private static final String SHA256 = "SHA-256";
    private static final int BYTE_SIZE = 8; // 8 BIT

    private byte[] message;
    private int numberBit;
    private final MessageDigest digest;

    Pow(byte[] message, int numberBit) throws NoSuchAlgorithmException {
        if (numberBit > 256)
            throw new IllegalArgumentException("Number of bits must be at max 256 bit.");
        this.digest = MessageDigest.getInstance(SHA256);
        this.message = message;
        this.numberBit = numberBit;
    }

    Pow(String message, int numberBit) throws NoSuchAlgorithmException {
        this(message.getBytes(StandardCharsets.UTF_8), numberBit);
    }

    public BigInteger compute() {
        BigInteger i = BigInteger.ZERO;

        while (true) {
            if (verify(i))
                return i;
            i = i.add(BigInteger.ONE);
        }
    }

    private boolean verify(BigInteger i) {
        byte[] concatMessageI = concat(message, i.toByteArray());
        byte[] encodedHash = digest(concatMessageI);
        int numberOfByte = (int) Math.ceil(numberBit / 8.0);

        for (int j = 0; j < numberOfByte; j++) {
            for (int t = 0; t < BYTE_SIZE; t++) {
                int bit = (encodedHash[j] >> t) & 1;
                if (j * BYTE_SIZE + t >= numberBit)
                    break;
                if (bit == 1)
                    return false;
            }
        }
        return true;
    }

    private byte[] concat(byte[] a, byte[] b) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(a);
            outputStream.write(b);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] digest(byte[] input) {
        byte[] hash = digest.digest(input);
        digest.reset();
        return hash;
    }
}
