package pt.ulisboa.tecnico.meic.sirs;

import javax.crypto.Cipher;
import java.security.Key;

public class RSACipherByteArrayMixer implements ByteArrayMixer {

    private String keyFile;
    private int opmode;

    public void setParameters(String keyFile) {
        this.keyFile = keyFile;
    }

    public RSACipherByteArrayMixer(int opmode) {
        this.opmode = opmode;
    }

    @Override
    public byte[] mix(byte[] byteArray, byte[] byteArray2) {

        try {
            Key key;
            if (opmode == Cipher.ENCRYPT_MODE)
                key = RSAKeyGenerator.readPublic(keyFile);
            else
                key = RSAKeyGenerator.readPrivate(keyFile);

            // prints what is available in the platform
            // for (Provider provider: Security.getProviders()) {
            //   System.out.println(provider.getName());
            //   for (String providerKey: provider.stringPropertyNames()) {
            //     String prop = provider.getProperty(providerKey);
            //     System.out.println("\t" + providerKey + "\t" + prop);
            //   }
            // }
            // get a RSA cipher object and print the provider
            // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            Cipher cipher = Cipher.getInstance("RSA"); // just RSA! ciphers 1 block
            System.out.println(cipher.getProvider().getInfo());


            System.out.println("Ciphering ...");
            cipher.init(this.opmode, key);

            // RSA input and output sizes are different
            int dataBlockSize = 64;
            int cipheredBlockSize = cipher.getOutputSize(dataBlockSize);
            int outBlockSize;
            int inBlockSize;
            int length;
            int remainder;
            int bufferSize;
            byte[] res;

            if (opmode == Cipher.ENCRYPT_MODE) {
                // output is the ciphertext
                outBlockSize = cipheredBlockSize;
                inBlockSize  = dataBlockSize;
            } else {
                // output is the plaintext
                outBlockSize = dataBlockSize;
                inBlockSize  = cipheredBlockSize;
            }

            length = byteArray.length / inBlockSize;
            remainder = byteArray.length % inBlockSize;
            bufferSize = (length * outBlockSize);/* + outBlockSize */; // always add some extra space
            res = new byte[bufferSize];

            // System.out.println("length: " + length + " remainder: "
            //   + remainder + " bufferSize: " + bufferSize + " inBlockSize: "
            //   + inBlockSize + " outBlockSize: " + outBlockSize);
            int inOffset  = 0;
            int outOffset = 0;
            int i = 0;
            for (; i < length - 1; i++, inOffset += inBlockSize, outOffset += outBlockSize) {
                // System.out.println("i: " + i + " inOffset: " + inOffset
                //   + " outOffset: " + outOffset);
                cipher.doFinal(byteArray, inOffset, inBlockSize, res, outOffset);
            }
            // System.out.println(" -- i: " + i + " inOffset: " + inOffset
            //   + " outOffset: " + outOffset);
            if (remainder > 0) {
                cipher.doFinal(byteArray, inOffset, remainder, res, outOffset);
            }
            return res;
        } catch (Exception e) {
            // Pokemon exception handling!
            e.printStackTrace();
        }

        return null;

    }
}
