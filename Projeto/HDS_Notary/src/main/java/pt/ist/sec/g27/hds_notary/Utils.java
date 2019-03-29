package pt.ist.sec.g27.hds_notary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] jsonObjectToByteArray(Object object) throws JsonProcessingException {
        log.info("Converting object to byte[].");
        return objectMapper.writeValueAsBytes(object);
    }
}
