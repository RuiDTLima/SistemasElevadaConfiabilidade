package pt.ist.sec.g27.hds_notary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ist.sec.g27.hds_notary.model.AppState;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@SpringBootApplication
public class HdsNotaryApplication {
    public final static String STATE_PATH = "state.json";
    public final static String BACKUP_STATE_PATH = "backup_state.json";
    private final static Logger log = LoggerFactory.getLogger(HdsNotaryApplication.class);

    private static AppState appState;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try (FileInputStream fileInputStream = new FileInputStream(STATE_PATH)) {
            appState = mapper.readValue(fileInputStream, AppState.class);
            log.info(String.format("Successfully read the state for notary with id %d", appState.getNotary().getId()));
        } catch (Exception e) {
            try (FileInputStream fileInputStream = new FileInputStream(BACKUP_STATE_PATH)) {
                appState = mapper.readValue(fileInputStream, AppState.class);
            } catch (Exception e1) {
                log.error("An error occurred.", e1);
                return;
            }
            try {
                Files.copy(Paths.get(BACKUP_STATE_PATH), Paths.get(STATE_PATH), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e1) {
                log.error("An error occurred while trying to copy the backup state to the current state.", e1);
            }
        }
        SpringApplication.run(HdsNotaryApplication.class, args);
    }

    @Bean
    public AppState getAppState() {
        return appState;
    }

    @Bean
    public int getNotaryId(AppState appState) {
        return appState.getNotary().getId();
    }
}
