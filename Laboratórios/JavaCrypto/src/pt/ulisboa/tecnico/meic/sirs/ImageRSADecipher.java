package pt.ulisboa.tecnico.meic.sirs;

import javax.crypto.Cipher;
import java.io.IOException;

public class ImageRSADecipher {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("This program decrypts an image file with RSA.");
            System.err.println("Usage: ImageRSADecipher [inputFile.png] [Private RSAKeyFile] [outputFile.png]");
            return;
        }

        final String inputFile = args[0];
        final String keyFile = args[1];
        final String outputFile = args[2];

        RSACipherByteArrayMixer cipher = new RSACipherByteArrayMixer(Cipher.DECRYPT_MODE);
        cipher.setParameters(keyFile);
        ImageMixer.mix(inputFile, outputFile, cipher);
    }
}
