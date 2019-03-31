package pt.ist.sec.g27.hds_notary.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.gov.cartaodecidadao.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtils {
    private final static Logger log = LoggerFactory.getLogger(SecurityUtils.class);
    private final static String ALGORITHM_FOR_VERIFY = "SHA256withRSA";

    public static PublicKey readPublic(String pubKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(pubKeyPath);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePublic(x509EncodedKeySpec);
    }

    // TODO remove this after testing and use sign with CC-PT
    public static PrivateKey readPrivate(String privKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded = read(privKeyPath);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePrivate(pkcs8EncodedKeySpec);
    }

    // TODO remove this after testing and use sign with CC-PT
    public static byte[] sign(PrivateKey privateKey, byte[] toSign) throws Exception {
        try {
            Signature signature = Signature.getInstance(ALGORITHM_FOR_VERIFY);
            signature.initSign(privateKey);
            signature.update(toSign);
            return signature.sign();
        } catch (Exception e) {
            log.warn("Something related to sign not worked properly.", e);
            throw e;// TODO change this to the correct exception
        }
    }

    private static byte[] read(String keyPath) throws IOException {
        ClassLoader classLoader = SecurityUtils.class.getClassLoader();
        String path = "keys/" + keyPath;
        URL resource = classLoader.getResource(path);
        log.info("Trying to obtain key from: " + path);
        try (FileInputStream inputStream = new FileInputStream(resource.getFile())) {
            byte[] encoded = new byte[inputStream.available()];
            log.info("Reading...");
            inputStream.read(encoded);
            return encoded;
        }
    }

    public static byte[] sign(byte[] toSign) throws Exception {
        log.info("Initialize library to sign.");
        PKCS11 pkcs11 = init();
        log.info("Initialize session to sign.");
        long p11_session = signInit(pkcs11);
        log.info("Signing...");
        return pkcs11.C_Sign(p11_session, toSign);
    }

    public static boolean verify(PublicKey publicKey, byte[] notSigned, byte[] signed) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM_FOR_VERIFY);
        log.info("Initialize signature verification.");
        signature.initVerify(publicKey);
        log.info("Updating signature.");
        signature.update(notSigned);
        log.info("Verifying signature.");
        return signature.verify(signed);
    }

    private static PKCS11 init() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, pt.gov.cartaodecidadao.PteidException {
        System.loadLibrary("pteidlibj");
        // Initializes the eID Lib
        pteid.Init("");
        // Don't check the integrity of the ID, address and photo (!)
        pteid.SetSODChecking(false);

        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");

        String libName = "libbeidpkcs11.so";

        if (osName.contains("Windows"))
            libName = "pteidpkcs11.dll";
        else if (osName.contains("Mac"))
            libName = "pteidpkcs11.dylib";
        Class<?> pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        if (javaVersion.startsWith("1.5.")) {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", String.class, CK_C_INITIALIZE_ARGS.class, boolean.class);
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, null, false});
        } else {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class);
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, "C_GetFunctionList", null, false});
        }
        return pkcs11;
    }

    private static long signInit(PKCS11 pkcs11) throws PKCS11Exception {
        //Open the PKCS11 session
        long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

        // Get available keys
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = PKCS11Constants.CKO_PRIVATE_KEY;

        pkcs11.C_FindObjectsInit(p11_session, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

        long signatureKey = keyHandles[0];
        pkcs11.C_FindObjectsFinal(p11_session);

        // initialize the signature method
        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA256_RSA_PKCS;
        mechanism.pParameter = null;
        pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
        return p11_session;
    }
}
