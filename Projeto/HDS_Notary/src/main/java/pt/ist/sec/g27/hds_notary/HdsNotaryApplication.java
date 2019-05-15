package pt.ist.sec.g27.hds_notary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pt.ist.sec.g27.hds_notary.model.AppState;
import pt.ist.sec.g27.hds_notary.model.Notary;
import pt.ist.sec.g27.hds_notary.utils.ByzantineReliableBroadcast;

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
    private static int notaryId;
    private static int byzantineFaultsLimit;

    public static Notary getMe() {
        return appState.getNotary(notaryId);
    }

    public static Notary[] getNotaries() {
        return appState.getNotaries();
    }

    public static int getNotariesAmount() {
        return appState.getNotaries().length;
    }

    public static int getByzantineFaultsLimit() {
        return byzantineFaultsLimit;
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (args.length < 2) {
                System.out.println("You need to specify your notary id and the number of byzantine faults to tolerate.");
                return;
            }
            notaryId = Integer.parseInt(args[0]);
            byzantineFaultsLimit = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("The argument needs to be an int.");
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(STATE_PATH)) {
            appState = mapper.readValue(fileInputStream, AppState.class);
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

    public static int getTotalNotaries() {
        return appState.getNotaries().length;
    }

    @Bean
    public AppState getAppState() {
        return appState;
    }

    @Bean
    public int getNotaryId() {
        return notaryId;
    }

    @Bean
    public ByzantineReliableBroadcast getBizantyneReliableBroadcast() {
        return new ByzantineReliableBroadcast(notaryId);
    }
}
