package pt.ulisboa.tecnico.meic.sirs;

import javax.crypto.Cipher;
import java.io.IOException;

public class ImageRSACipher {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("This program encrypts an image file with RSA.");
            System.err.println("Usage: ImageRSACipher [inputFile.png] [Public RSAKeyFile] [outputFile.png]");
            return;
        }

        final String inputFile = args[0];
        final String keyFile = args[1];
        final String outputFile = args[2];

        RSACipherByteArrayMixer cipher = new RSACipherByteArrayMixer(Cipher.ENCRYPT_MODE);
        cipher.setParameters(keyFile);
        try {
            ImageMixer.mixRSA(inputFile, outputFile, cipher);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
