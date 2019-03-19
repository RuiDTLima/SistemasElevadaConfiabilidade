package pt.ist.sec.g27.hds_notary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ist.sec.g27.hds_notary.model.Notary;

import java.io.IOException;

@SpringBootApplication
public class HdsNotaryApplication {
    private final static String STATE_PATH = "state.json";
    private final static Logger log = LoggerFactory.getLogger(HdsNotaryApplication.class);

    private static Notary notary;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ClassLoader classLoader = HdsNotaryApplication.class.getClassLoader();
            notary = mapper.readValue(classLoader.getResource(STATE_PATH), Notary.class);
        } catch (IOException e) {
            log.error("An error occurred.", e);
            return;
        }
        SpringApplication.run(HdsNotaryApplication.class, args);
    }

    @Bean
    public Notary getNotary() {
        return notary;
    }
}
