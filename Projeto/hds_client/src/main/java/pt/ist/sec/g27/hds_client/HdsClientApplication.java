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
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;
import pt.ist.sec.g27.hds_client.utils.Utils;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class HdsClientApplication {
    private static final String STATE_PATH = "state.json";
    private static final Logger log = LoggerFactory.getLogger(HdsClientApplication.class);
    private static final RestClient restClient = new RestClient();
    private static final String YES = "YES";
    private static final String GET_STATE_OF_GOOD_URL = "/getStateOfGood";
    private static final String INTENTION_TO_SELL_URL = "/intentionToSell";
    private static final String BUY_GOOD_URL = "/buyGood";
    private static final String UPDATE_URL = "/update";

    private static AppState appState;
    private static User me;
    private static Notary[] notaries;
    private static int byzantineFaultsLimit;
    private static int numberOfNotaries;

    private int rId;
    private Value[] readList;
    private boolean[] ackList;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static User getMe() {
        return me;
    }

    public static User getUser(int userId) {
        return appState.getUser(userId);
    }

    public static Notary[] getNotaries() {
        return notaries;
    }

    public static Notary getNotary(int notaryId) {
        return appState.getNotary(notaryId);
    }

    public static Good getGood(int goodId) {
        return appState.getGood(goodId);
    }

    public static int getByzantineFaultsLimit() {
        return byzantineFaultsLimit;
    }

    public static int getNumberOfNotaries() {
        return numberOfNotaries;
    }

    public static void addTransferCertificate(TransferCertificate transferCertificate) {
        appState.addTransferCertificate(transferCertificate);
    }

    public static void main(String[] args) {
        int userId;
        try {
            if (args.length < 2) {
                System.out.println("You need to specify your user id and the number of byzantine faults to tolerate.");
                return;
            }
            userId = Integer.parseInt(args[0]);
            byzantineFaultsLimit = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("The argument needs to be an int.");
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(STATE_PATH)) {
            appState = mapper.readValue(fileInputStream, AppState.class);
        } catch (Exception e) {
            log.error("An error occurred.", e);
            return;
        }

        me = appState.getUser(userId);
        Scanner scanner = new Scanner(System.in);
        String password = "";

        try {
            do {
                String msg = "Please insert password: ";
                System.out.print(msg);
                System.out.flush();

                try {
                    password = scanner.next();
                } catch (Exception e) {
                    msg = "Something went wrong while trying to read the password.";
                    log.warn(msg);
                    System.out.println(msg);
                }
            } while (!me.validateUser(password));
        } catch (Exception e) {
            log.warn(e.getMessage());
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("Password is correct.");

        notaries = appState.getNotaries();
        numberOfNotaries = notaries.length;

        if (byzantineFaultsLimit >= ((3 * numberOfNotaries) + 1)) {
            String errorMessage = String.format("The value of acceptable byzantine faults must be at most (3*N)+1 where N is number of notaries which is currently %d", numberOfNotaries);
            log.warn(errorMessage);
            System.out.println(errorMessage);
            return;
        }

        SpringApplication.run(HdsClientApplication.class, args);

        HdsClientApplication hdsClientApplication = new HdsClientApplication();
        hdsClientApplication.init();
        hdsClientApplication.run();
    }

    private void init() {
        rId = 0;
        readList = new Value[numberOfNotaries];
        ackList = new boolean[numberOfNotaries];
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
            case "getStateOfGood":
                if (validateParams(params,
                        1,
                        String.format("To invoke getStateOfGood there needs to be passed one id. It was passed %d ids.", params.length),
                        "To invoke getStateOfGood there needs to be passed one id."))
                    getStateOfGood(params);
                break;
            case "intentionToSell":
                if (validateParams(params,
                        1,
                        String.format("To invoke intentionToSell there needs to be passed one id. It was passed %d ids.", params.length),
                        "To invoke intentionToSell there needs to be passed one id."))
                    intentionToSell(params);
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

    private void getStateOfGood(String[] params) throws Exception {
        int goodId = Integer.parseInt(params[0]);

        if (!goodExist(goodId))
            return;

        rId++;
        readList = new Value[numberOfNotaries];
        Body body = new Body(me.getId(), goodId, rId, true);

        List<Message> receivedMessages = makeRequestToMultipleNotaries(notaries, GET_STATE_OF_GOOD_URL, body);

        if (receivedMessages == null)
            return;

        int receives = 0;
        for (Message receivedMessage : receivedMessages) {
            Body receivedBody = receivedMessage.getBody();

            if (receivedBody != null) {
                int notaryId = receivedBody.getSenderId();
                Notary notary = appState.getNotary(notaryId);
                if (Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage) && receivedBody.getrId() == rId) {
                    if (receivedBody.getStatus().is2xxSuccessful()) {
                        int userId = receivedBody.getUserId();
                        int signedId = receivedBody.getSignedId();
                        User signedUser = getUser(signedId);
                        if (signedUser == null)
                            continue;
                        Good good = getGood(goodId);
                        if (good == null)
                            continue;
                        Good receivedGood = new Good(goodId, userId, good.getName(), State.getStateFromString(receivedBody.getState()), receivedBody.getwTs(), signedId);
                        if (!SecurityUtils.verify(signedUser.getPublicKey(), Utils.jsonObjectToByteArray(receivedGood), receivedBody.getSignature()))
                            continue;
                        readList[notaryId] = new Value(receivedBody.getwTs(), receivedBody);
                        receives++;
                    } else {
                        readList[notaryId] = new Value(receivedBody.getwTs(), receivedBody);
                        receives++;
                    }
                    if (receives > (numberOfNotaries + byzantineFaultsLimit) / 2) {
                        int higher = -1;
                        Body toReturn = null;
                        for (Value currentValue : readList) {
                            if (currentValue != null) {
                                if(!currentValue.getValue().getStatus().is2xxSuccessful() || currentValue.getTimestamp() > higher) {
                                    toReturn = currentValue.getValue();
                                    higher = currentValue.getTimestamp();
                                }
                            }
                        }
                        readList = new Value[numberOfNotaries];
                        if (toReturn == null)
                            continue;
                        if (!toReturn.getStatus().is2xxSuccessful()) {
                            log.info(toReturn.getResponse());
                            System.out.println(toReturn.getResponse());
                            return;
                        }

                        updateNotaries(toReturn);

                        String message = String.format("The good with id %d is owned by user with id %d and his state is %s.",
                                body.getGoodId(),
                                toReturn.getUserId(),
                                toReturn.getState());

                        log.info(message);
                        System.out.println(message);
                        return;
                    }
                }
            }
        }
        String errorMessage = "Did not received a valid response.";
        log.info(errorMessage);
        System.out.println(errorMessage);
    }

    private void intentionToSell(String[] params) throws Exception {
        int goodId = Integer.parseInt(params[0]);

        Good good = appState.getGood(goodId);

        if (good == null)
            return;

        good.incrWts();
        int wTs = good.getwTs();
        ackList = new boolean[numberOfNotaries];
        byte[] sigma = SecurityUtils.sign(me.getPrivateKey(), Utils.jsonObjectToByteArray(new Good(goodId, me.getId(), good.getName(), State.ON_SALE, wTs, me.getId())));
        Body body = new Body(me.getId(), goodId, wTs, false, sigma);

        List<Message> receivedMessages = makeRequestToMultipleNotaries(notaries, INTENTION_TO_SELL_URL, body);
        if (receivedMessages == null)
            return;

        int receives = 0;
        int yesReceives = 0, noReceives = 0, invalidReceives = 0;
        Body yesBody = null, noBody = null, invalidBody = null;

        List<Integer> receivedwTs = new ArrayList<>();
        for (Message receivedMessage : receivedMessages) {
            Body receivedBody = receivedMessage.getBody();

            if (receivedBody != null) {
                int notaryId = receivedBody.getSenderId();
                Notary notary = appState.getNotary(notaryId);

                if (notary != null && Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage)) {
                    int currentwTs = receivedBody.getwTs();
                    receivedwTs.add(currentwTs);
                    if (currentwTs == wTs) {
                        ackList[notaryId] = true;
                        receives++;

                        if (!receivedBody.getStatus().is2xxSuccessful()) {
                            invalidReceives++;
                            invalidBody = receivedBody;
                        } else if (receivedBody.getResponse().equals(YES)) {
                            yesReceives++;
                            yesBody = receivedBody;
                        } else {
                            noReceives++;
                            noBody = receivedBody;
                        }

                        if (receives > (numberOfNotaries + byzantineFaultsLimit) / 2) {
                            ackList = new boolean[numberOfNotaries];

                            if (yesReceives > noReceives && yesReceives > invalidReceives) {
                                log.info(String.format("The good with id %d is on sale.", goodId));
                                System.out.println(yesBody.getResponse());
                                return;
                            } else if (noReceives > invalidReceives) {
                                log.info(noBody.getResponse());
                                System.out.println(noBody.getResponse());
                                return;
                            }
                            log.info(invalidBody.getResponse());
                            System.out.println(invalidBody.getResponse());
                            return;
                        }
                    }
                }
            }
        }
        if (receivedwTs.size() > (numberOfNotaries + byzantineFaultsLimit) / 2) {
            int max = Collections.max(receivedwTs);
            good.setwTs(max);
            intentionToSell(params);
            return;
        }
        String errorMessage = "There was no valid responses.";
        log.info(errorMessage);
        System.out.println(errorMessage);
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
        if (owner == null) {
            String errorMessage = String.format("The user with id %d does not exist.", userId);
            log.info(errorMessage);
            System.out.println(errorMessage);
            return;
        }

        Good good = appState.getGood(goodId);

        if (good == null) {
            String errorMessage = String.format("The good with id %d does not exist.", goodId);
            log.info(errorMessage);
            System.out.println(errorMessage);
            return;
        }

        Body body = new Body(me.getId(), goodId);
        Message receivedMessage = makeRequest(owner, BUY_GOOD_URL, body);
        if (receivedMessage == null)
            return;

        Utils.verifyAllMessages(receivedMessage, BUY_GOOD_URL);

        Body notaryBody = receivedMessage.getBody().getMessage().getBody();

        String response = notaryBody.getResponse();
        if (response.equals(YES)) {
            good.setwTs(notaryBody.getwTs());
            addTransferCertificate(notaryBody.getTransferCertificate());
        }
        log.info(response);
        System.out.println(response);
    }

    private void updateNotaries(Body body) throws Exception {
        body.setrId(rId);
        List<Message> receivedMessages = makeRequestToMultipleNotaries(notaries, UPDATE_URL, body);

        if (receivedMessages == null)
            return;

        int acks = 0;
        for (Message receivedMessage : receivedMessages) {
            Body receivedBody = receivedMessage.getBody();

            if (receivedBody != null) {
                int notaryId = receivedBody.getSenderId();
                Notary notary = appState.getNotary(notaryId);
                if (Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage) && receivedBody.getrId() == rId) {
                    acks++;

                    if (acks > (numberOfNotaries / 2)) {
                        return;
                    }
                }
            }
        }
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

    private List<Message> makeRequestToMultipleNotaries(Notary[] notaries, String uri, Body body) throws Exception {
        try {
            return restClient.postToMultipleNotaries(notaries, uri, body, me.getPrivateKey());
        } catch (UnverifiedException | ResponseException e) {
            log.warn(e.getMessage(), e);
            return null;
        } catch (ConnectionException e) {
            System.out.println("It wasn't possible to connect to the server.");
            log.warn(e.getMessage(), e);
            return null;
        }
    }
}