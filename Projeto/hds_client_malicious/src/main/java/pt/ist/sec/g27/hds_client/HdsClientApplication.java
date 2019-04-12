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
import pt.ist.sec.g27.hds_client.utils.Utils;

import java.io.FileInputStream;
import java.util.Scanner;

@SpringBootApplication
public class HdsClientApplication {
    private static final String STATE_PATH = "state.json";
    private static final Logger log = LoggerFactory.getLogger(HdsClientApplication.class);
    private static final RestClient restClient = new RestClient();

    private static AppState appState;
    private static User me;
    private static User notary;

    private User lastDestinyUser;
    private String lastUri;
    private Body lastBody;

    public static User getMe() {
        return me;
    }

    public static User getNotary() {
        return notary;
    }

    public static User getUser(int userId) {
        return appState.getUser(userId);
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

        try (FileInputStream fileInputStream = new FileInputStream(STATE_PATH)) {
            appState = mapper.readValue(fileInputStream, AppState.class);
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
                    System.out.println(e.getMessage());
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
            case "sendLastMessage":
                if (validateParams(params,
                        0,
                        String.format("To invoke sendLastMessage there is no need to pass something. It was passed %d params.", params.length),
                        "To invoke sendLastMessage there is no need to pass something."))
                    sendLastMessage();
                break;
            case "exit":
                log.info("Shutting down the application.");
                System.exit(0);
                break;
            default:
                log.info(String.format("Command %s not found.", command));
                System.out.println("Unknown command.");
                break;
        }
    }

    private void intentionToSell(String[] params) throws Exception {
        String uri = "/intentionToSell";
        int goodId = Integer.parseInt(params[0]);

        if (!goodExist(goodId))
            return;

        Body body = new Body(me.getId(), goodId);

        Message receivedMessage = makeRequest(notary, uri, body);
        if (receivedMessage == null)
            return;

        lastDestinyUser = notary;
        lastUri = uri;
        lastBody = body;

        Body receivedBody = receivedMessage.getBody();

        if (isValidResponse(receivedBody) && Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage)) {
            notary.setTimestamp(receivedBody.getTimestamp());
            log.info(String.format("The good with id %d is on sale.", body.getGoodId()));
            System.out.println(receivedBody.getResponse());
            return;
        }

        String errorMessage = "Could not verify the message";
        log.info(errorMessage);
        System.out.println(errorMessage);
    }

    private void getStateOfGood(String[] params) throws Exception {
        String uri = "/getStateOfGood";
        int goodId = Integer.parseInt(params[0]);

        if (!goodExist(goodId))
            return;

        Body body = new Body(me.getId(), goodId);
        Message receivedMessage = makeRequest(notary, uri, body);
        if (receivedMessage == null)
            return;

        lastDestinyUser = notary;
        lastUri = uri;
        lastBody = body;

        Body receivedBody = receivedMessage.getBody();

        if (isValidResponse(receivedBody) && Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage)) {
            notary.setTimestamp(receivedBody.getTimestamp());
            String message = String.format("The good with id %d is owned by user with id %d and his state is %s.",
                    body.getGoodId(),
                    receivedBody.getUserId(),
                    receivedBody.getState());

            log.info(message);
            System.out.println(message);
            return;
        }

        String errorMessage = "Could not verify the message";
        log.info(errorMessage);
        System.out.println(errorMessage);
    }

    private void buyGood(String[] params) throws Exception {
        String uri = "/buyGood";
        int goodId, userId;

        try {
            goodId = Integer.parseInt(params[0]);
            userId = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            log.warn("The provided id was not an integer.");
            System.out.println("The id you provide must be an integer.");
            return;
        }

        if (userId == me.getId()) {
            String errorMessage = "The user cannot buy from itself.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return;
        }

        User owner = appState.getUser(userId);
        if (owner == null) {
            String errorMessage = String.format("The user with id %d does not exist.", userId);
            log.info(errorMessage);
            System.out.println(errorMessage);
            return;
        }

        if (!goodExist(goodId))
            return;

        Body body = new Body(me.getId(), goodId);

        lastDestinyUser = owner;
        lastUri = uri;
        lastBody = body;

        Message receivedMessage = makeRequest(owner, uri, body);
        if (receivedMessage == null)
            return;

        Utils.verifyAllMessages(receivedMessage, uri);

        Body notaryBody = receivedMessage.getBody().getMessage().getBody();

        notary.setTimestamp(notaryBody.getTimestamp());
        addTransferCertificate(notaryBody.getTransferCertificate());

        String response = notaryBody.getResponse();
        log.info(response);
        System.out.println(response);
    }

    private void sendLastMessage() throws Exception {
        if (lastDestinyUser == null) {
            String errorMessage = "This is the first message. There's not a previous one to repeat.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return;
        }

        log.info("Repeating the previous message.");
        makeRequest(lastDestinyUser, lastUri, lastBody);
    }

    private boolean validateParams(String[] params, int length, String logMessage, String outputMessage) {
        if (params.length == length)
            return true;

        log.info(logMessage);
        System.out.println(outputMessage);
        return false;
    }

    private boolean goodExist(int goodId) {
        Good good = appState.getGood(goodId);
        if (good == null) {
            String errorMessage = String.format("The good with id %d does not exist.", goodId);
            log.info(errorMessage);
            System.out.println(errorMessage);
            return false;
        }
        return true;
    }

    private Message makeRequest(User user, String uri, Body body) throws Exception {
        try {
            return restClient.post(user, uri, body, me.getPrivateKey());
        } catch (UnverifiedException | ResponseException e) {
            log.warn(e.getMessage(), e);
            return null;
        } catch (ConnectionException e) {
            System.out.println("It wasn't possible to connect to the server.");
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    private boolean isValidResponse(Body body) {
        if (body == null) {
            String errorMessage = "The server could not respond.";
            log.info(errorMessage);
            System.out.println(errorMessage);
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