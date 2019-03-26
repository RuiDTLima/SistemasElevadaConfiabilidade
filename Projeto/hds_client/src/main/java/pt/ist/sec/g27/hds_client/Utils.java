package pt.ist.sec.g27.hds_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static byte[] objectToByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            log.warn("Your object needs to implement the interface Serializable. Your object is: " + object.toString(), e);
            throw e;
        }
    }
}
