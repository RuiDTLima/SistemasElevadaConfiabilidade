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

            // get a RSA cipher object and print the provider
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            System.out.println(cipher.getProvider().getInfo());


            System.out.println("Ciphering ...");
            cipher.init(this.opmode, key);

            int blockSize = 62;
            int length = byteArray.length / blockSize;
            int padding = byteArray.length % blockSize;
            byte[] res = new byte[(length * 256) + 256];
            int offset = 0;
            for (int i = 0; i < length; i++, offset += blockSize) {
                cipher.doFinal(byteArray, offset, blockSize, res, offset);
            }
            cipher.doFinal(byteArray, offset, padding, res, offset);
            return res;

        } catch (Exception e) {
            // Pokemon exception handling!
            e.printStackTrace();
        }

        return null;

    }
}
