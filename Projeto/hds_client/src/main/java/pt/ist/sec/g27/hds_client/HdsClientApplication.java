package pt.ist.sec.g27.hds_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pt.ist.sec.g27.hds_client.exceptions.ConnectionException;
import pt.ist.sec.g27.hds_client.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.model.*;

import java.util.Scanner;
import java.util.stream.Stream;

@SpringBootApplication
public class HdsClientApplication {
    private static final String STATE_PATH = "state.json";
    private static final Logger log = LoggerFactory.getLogger(HdsClientApplication.class);
    private static final RestClient restClient = new RestClient();

    private static AppState appState;
    private static User me;
    private static User notary;

    public static User getMe() {
        return me;
    }

    public static Stream<Good> getMyGoods() {
        return appState.getUsersGood(me.getId());
    }

    public static User getNotary() {
        return notary;
    }

    public static void addTransferCertificate(TransferCertificate transferCertificate) {
        appState.addTransferCertificate(transferCertificate);
    }

    public static void main(String[] args) {
        int userId;
        try {
            if (args.length == 0) {
                System.out.println("You need to specify your user id.");
                return;
            }
            userId = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("The argument needs to be an int.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            ClassLoader classLoader = HdsClientApplication.class.getClassLoader();
            appState = mapper.readValue(classLoader.getResource(STATE_PATH), AppState.class);
        } catch (Exception e) {
            log.error("An error occurred.", e);
            return;
        }

        me = appState.getUser(userId);
        if (!me.validateUser()) {
            log.warn("The user tried to access the system with an invalid state.");
            System.out.println("The private key to the given user was not provided in the state.");
            return;
        }

        notary = appState.getNotary();

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

                    commands(command, params);
                } catch (Throwable e) {
                    log.warn("Something went wrong.", e);
                    if (!input.equals(""))
                        log.warn(input);
                }
            }
        }
    }

    private void commands(String command, String[] params) throws Exception {
        switch (command) {
            case "intentionToSell":
                if (validateParams(params,
                        1,
                        String.format("To invoke intentionToSell there needs to be passed one id. It was passed %d ids.", params.length),
                        "To invoke intentionToSell there needs to be passed one id."))
                    intentionToSell(params);
                break;
            case "getStateOfGood":
                if (validateParams(params,
                        1,
                        String.format("To invoke getStateOfGood there needs to be passed one id. It was passed %d ids.", params.length),
                        "To invoke getStateOfGood there needs to be passed one id."))
                    getStateOfGood(params);
                break;
            case "buyGood":
                if (validateParams(params,
                        2,
                        String.format("To invoke buyGood there needs to be passed two ids. It was passed %d ids.", params.length),
                        "To invoke buyGood there needs to be passed two ids, the id of the good and the owner, respectively."))
                    buyGood(params);
                break;
            case "exit":
                System.exit(0);
                break;
            default:
                log.info(String.format("Command %s not found.", command));
                System.out.println("Unknown command.");
                break;
        }
    }

    private void intentionToSell(String[] params) throws Exception {
        Body body = new Body(me.getId(), Integer.parseInt(params[0]));
        Message receivedMessage;

        try {
            receivedMessage = restClient.post(notary, "/intentionToSell", body, me.getPrivateKey());
        } catch (UnverifiedException | ResponseException e) {
            log.warn(e.getMessage(), e);
            return;
        } catch (ConnectionException e) {
            System.out.println("It wasn't possible to connect to the server.");
            log.warn(e.getMessage(), e);
            return;
        }

        Body receivedBody = receivedMessage.getBody();

        if (isValidResponse(receivedBody)) {
            log.info(String.format("The good with id %d is on sale.", body.getGoodId()));
            System.out.println(receivedBody.getResponse());
        }
    }

    private void getStateOfGood(String[] params) throws Exception {
        Body body = new Body(me.getId(), Integer.parseInt(params[0]));
        Message receivedMessage;

        try {
            receivedMessage = restClient.post(notary, "/getStateOfGood", body, me.getPrivateKey());
        } catch (UnverifiedException | ResponseException e) {
            log.info(e.getMessage(), e);
            return;
        } catch (ConnectionException e) {
            System.out.println("It wasn't possible to connect to the server.");
            log.warn(e.getMessage(), e);
            return;
        }

        Body receivedBody = receivedMessage.getBody();

        if (isValidResponse(receivedBody)) {
            String message = String.format("The good with id %d is owned by user with id %d and his state is %s.",
                    body.getGoodId(),
                    receivedBody.getUserId(),
                    receivedBody.getState());

            log.info(message);
            System.out.println(message);
        }
    }

    private void buyGood(String[] params) throws Exception {
        int goodId, userId;

        try {
            goodId = Integer.parseInt(params[0]);
            userId = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            log.warn("The provided id was not an integer.");
            System.out.println("The id you provide must be an integer.");
            return;
        }

        User owner = appState.getUser(userId);
        Body body = new Body(userId, goodId);

        Message receivedMessage;

        try {
            receivedMessage = restClient.post(owner, "/buyGood", body, me.getPrivateKey());
        } catch (UnverifiedException | ResponseException e) {
            log.warn(e.getMessage(), e);
            return;
        } catch (ConnectionException e) {
            System.out.println("It wasn't possible to connect to the server.");
            log.warn(e.getMessage(), e);
            return;
        }

        Body receivedBody = receivedMessage.getBody();

        if (isValidResponse(receivedBody)) {
            log.info(String.format("The buy operation of the good with id %d from the user with id %d return the response %s", body.getGoodId(), body.getUserId(), receivedBody.getResponse()));
            System.out.println(receivedBody.getResponse());
        }
    }

    private boolean validateParams(String[] params, int length, String logMessage, String outputMessage) {
        if (params.length == length)
            return true;

        log.info(logMessage);
        System.out.println(outputMessage);
        return false;
    }

    private boolean isValidResponse(Body body) {
        if (body == null) {
            String unsignedMessage = "The server could not respond.";
            log.info(unsignedMessage);
            System.out.println(unsignedMessage);
            return false;
        }

        if (!body.getStatus().is2xxSuccessful()) {
            log.info(body.getResponse());
            System.out.println(body.getResponse());
            return false;
        }

        if (body.getTimestampInUTC().compareTo(notary.getTimestampInUTC()) <= 0) {
            String errorMessage = "The message received is a repeat of a previous one.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return false;
        }

        return true;
    }
}