package pt.ist.sec.g27.hds_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pt.ist.sec.g27.hds_client.model.AppState;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Me;
import pt.ist.sec.g27.hds_client.model.User;

import java.io.IOException;
import java.util.Scanner;

@SpringBootApplication
public class HdsClientApplication {
    private final static String STATE_PATH = "state.json";
    private static final Logger log = LoggerFactory.getLogger(HdsClientApplication.class);
    private static final RestClient restClient = new RestClient();

    private static AppState appState;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ClassLoader classLoader = HdsClientApplication.class.getClassLoader();
            appState = mapper.readValue(classLoader.getResource(STATE_PATH), AppState.class);
        } catch (IOException e) {
            log.error("An error occurred.", e);
            return;
        }
        SpringApplication.run(HdsClientApplication.class, args);
        HdsClientApplication hdsClientApplication = new HdsClientApplication();
        hdsClientApplication.run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = "";
                try {
                    input = scanner.nextLine();
                    String[] temp = input.split(" ");
                    String command = temp[0];
                    String[] params = new String[temp.length - 1];
                    System.arraycopy(temp, 1, params, 0, params.length);
                    log.info(String.format("Received the input %s.", input));

                    switch (command) {
                        case "intentionToSell":
                            if (!validateParams(params, 1)) {
                                log.info(String.format("To invoke intentionToSell there need to be passed one id. It was passed %d ids.", params.length));
                                System.out.println("To invoke intentionToSell there need to be passed one id.");
                                break;
                            }
                            intentionToSell(params);
                            break;
                        case "getStateOfGood":
                            if (!validateParams(params, 1)) {
                                log.info(String.format("To invoke getStateOfGood there need to be passed one id. It was passed %d ids.", params.length));
                                System.out.println("To invoke getStateOfGood there need to be passed one id.");
                                break;
                            }
                            getStateOfGood(params);
                            break;
                        case "buyGood":
                            if (!validateParams(params, 2)) {
                                log.info(String.format("To invoke buyGood there need to be passed two ids. It was passed %d ids.", params.length));
                                System.out.println("To invoke buyGood there need to be passed two ids.");
                                break;
                            }
                            buyGood(params);
                            break;
                        case "transferGood":
                            if (!validateParams(params, 2)) {
                                log.info(String.format("To invoke transferGood there need to be passed two ids. It was passed %d ids.", params.length));
                                System.out.println("To invoke transferGood there need to be passed two ids.");
                                break;
                            }
                            transferGood(params);
                            break;
                        case "exit":
                            System.exit(0);
                            break;
                        default:
                            log.info(String.format("Command %s not found.", command));
                            System.out.println("Unknown command.");
                            break;
                    }
                } catch (Throwable e) {
                    log.warn("Something went wrong.", e);
                    if (!input.equals(""))
                        log.warn(input);
                }
            }
        }
    }

    private void intentionToSell(String[] params) throws Exception {
        Me me = appState.getMe();
        Body body = new Body(me.getId(), Integer.parseInt(params[0]));
        User notary = appState.getNotary();
        Body receivedBody;

        try {
            receivedBody = restClient.post(notary, "/intentionToSell", body, me.getPrivateKey());
        } catch (UnverifiedException e) {
            log.info(e.getMessage(), e);
            throw e;
        }

        if (receivedBody == null) {
            String unsignedMessage = "The server cannot respond.";
            log.info(unsignedMessage);
            System.out.println(unsignedMessage);
        } else if (receivedBody.getExceptionResponse() != null) {
            log.info(receivedBody.getExceptionResponse());
            System.out.println(receivedBody.getExceptionResponse());
        } else {
            log.info(String.format("The good with id %d is on sale.", body.getGoodId()));
            System.out.println(receivedBody.getResponse());
        }
    }

    private void getStateOfGood(String[] paramsd) {

    }

    private void buyGood(String[] params) {

    }

    private void transferGood(String[] params) {

    }

    private boolean validateParams(String[] params, int length) {
        return params.length == length;
    }
}
