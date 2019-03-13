package pt.ulisboa.tecnico.meic.sirs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class RSAKeyGenerator {

    public static void main(String[] args) throws Exception {

        // check args
        if (args.length != 3) {
            System.err.println("Usage: RSAKeyGenerator [r|w] <priv-key-file> <pub-key-file>");
            return;
        }

        final String mode = args[0];
        final String privkeyPath = args[1];
        final String pubkeyPath = args[2];

        if (mode.toLowerCase().startsWith("w")) {
            System.out.println("Generate and save keys");
            write(privkeyPath, pubkeyPath);
        } else {
            System.out.println("Load keys");
            Key privKey = readPrivate(privkeyPath);
            System.out.println("Private Key:");
            System.out.println(printHexBinary(privKey.getEncoded()));
            Key pubKey = readPublic(pubkeyPath);
            System.out.println("Public Key:");
            System.out.println(printHexBinary(pubKey.getEncoded()));
        }

        System.out.println("Done.");
    }

    public static void write(String privkeyPath, String pubkeyPath) throws GeneralSecurityException, IOException {
        // get an AES private key
        System.out.println("Generating RSA key ...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keys = keyGen.generateKeyPair();
        System.out.println("Finish generating RSA keys");

        System.out.println("Private Key:");
        PrivateKey privKey = keys.getPrivate();
        byte[] privKeyEncoded = privKey.getEncoded();
        System.out.println(printHexBinary(privKeyEncoded));
        System.out.println("Public Key:");
        PublicKey pubKey = keys.getPublic();
        byte[] pubKeyEncoded = pubKey.getEncoded();
        System.out.println(printHexBinary(pubKeyEncoded));
        System.out.println("Writing Private key to '" + privkeyPath + "' ...");
        FileOutputStream privFos = new FileOutputStream(privkeyPath);
        privFos.write(privKeyEncoded);
        privFos.close();
        System.out.println("Writing Public key to '" + pubkeyPath + "' ...");
        FileOutputStream pubFos = new FileOutputStream(pubkeyPath);
        pubFos.write(pubKeyEncoded);
        pubFos.close();
    }

    private static byte[] read(String keyPath) throws IOException {
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();
        return encoded;
    }

    public static Key readPrivate(String keyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Reading private key from file " + keyPath + " ...");
        byte[] encoded = read(keyPath);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePrivate(pkcs8EncodedKeySpec);
    }

    public static Key readPublic(String keyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Reading public key from file " + keyPath + " ...");
        byte[] encoded = read(keyPath);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePublic(x509EncodedKeySpec);
    }

}
