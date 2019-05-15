package pt.ist.sec.g27.hds_notary.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pt.ist.sec.g27.hds_notary.HdsNotaryApplication;
import pt.ist.sec.g27.hds_notary.exceptions.NotFoundException;
import pt.ist.sec.g27.hds_notary.model.Body;
import pt.ist.sec.g27.hds_notary.model.Message;
import pt.ist.sec.g27.hds_notary.model.Notary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ByzantineReliableBroadcast {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DELIVER_URL = "/deliver";
    private static final String ECHO_URL = "/echo";
    private static final String READY_URL = "/ready";
    private final static Logger log = LoggerFactory.getLogger(ByzantineReliableBroadcast.class);

    private boolean sentEcho;
    private boolean sentReady;
    private boolean delivered;
    private Message[] echos;
    private int countEchos;
    private Message[] readys;
    private int countReadys;
    private int notaryId;

    @Autowired
    public ByzantineReliableBroadcast(int notaryId) {
        this.notaryId = notaryId;
    }

    public void init(Message message) {
        sentEcho = false;
        sentReady = false;
        delivered = false;
        echos = new Message[HdsNotaryApplication.getTotalNotaries()];
        countEchos = 0;
        readys = new Message[HdsNotaryApplication.getTotalNotaries()];
        countReadys = 0;

        List<CompletableFuture<Optional<Message>>> completableFutures = broadcast(message);
        log.info("Waiting for responses.");
        for (CompletableFuture<Optional<Message>> futures : completableFutures) {
            futures.join();
        }
    }

    private List<CompletableFuture<Optional<Message>>> broadcast(Message message) {
        log.info(String.format("Broadcast message from notary with id %d to all notaries", HdsNotaryApplication.getMe().getId()));
        return toAllNotaries(DELIVER_URL, message);
    }

    public void send(Message message) {
        log.info(String.format("Send method in notary with id %d", HdsNotaryApplication.getMe().getId()));
        if (message == null || message.getBody() == null) {
            String errorMessage = "The message or its body cannot be null.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }
        if (message.getBody().getSenderId() == HdsNotaryApplication.getMe().getId() && !sentEcho) {
            sentEcho = true;
            toAllNotaries(ECHO_URL, message);
        }
    }

    public void echo(Message message) {
        log.info(String.format("Echo method in notary with id %d", HdsNotaryApplication.getMe().getId()));
        if (message == null || message.getBody() == null) {
            String errorMessage = "The message or its body cannot be null.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }
        if (echos[message.getBody().getSenderId()] == null) {
            log.info(String.format("Received echo from notary with id %d for the first time.", message.getBody().getSenderId()));
            echos[message.getBody().getSenderId()] = message;
            countEchos++;
        }

        if (countEchos > ((HdsNotaryApplication.getTotalNotaries() + HdsNotaryApplication.getByzantineFaultsLimit()) / 2) && !sentReady) {
            sentReady = true;
            toAllNotaries(READY_URL, message);
        }
    }

    public void ready(Message message) {
        log.info(String.format("Ready method in notary with id %d", HdsNotaryApplication.getMe().getId()));
        if (message == null || message.getBody() == null) {
            String errorMessage = "The message or its body cannot be null.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }
        if (readys[message.getBody().getSenderId()] == null) {
            log.info(String.format("Received ready from notary with id %d for the first time.", message.getBody().getSenderId()));
            readys[message.getBody().getSenderId()] = message;
            countReadys++;
        }

        if (countReadys > HdsNotaryApplication.getByzantineFaultsLimit() && !sentReady) {
            sentReady = true;
            toAllNotaries(READY_URL, message);
        }

        while(true) {
            if (message != null && countReadys > (2 * HdsNotaryApplication.getByzantineFaultsLimit()) && !delivered) {
                delivered = true;
                return;
            }
        }
    }

    private List<CompletableFuture<Optional<Message>>> toAllNotaries(String uri, Message message) {
        Notary[] notaries = HdsNotaryApplication.getNotaries();
        Body body = new Body(notaryId, message);
        byte[] jsonBody = new byte[0];
        try {
            jsonBody = mapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Message toSendMessage = null;
        try {
            toSendMessage = new Message(body, SecurityUtils.sign(SecurityUtils.readPrivate(HdsNotaryApplication.getMe().getPrivateKeyPath()), jsonBody));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json = null;
        try {
            json = mapper.writeValueAsString(toSendMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        List<CompletableFuture<Optional<Message>>> completableFutures = new ArrayList<>();
        try (AsyncHttpClient asyncClient = Dsl.asyncHttpClient()) {

            for (int i = 0; i < notaries.length; i++) {
                String url = notaries[i].getUrl() + uri;
                completableFutures.add(asyncClient.preparePost(url)
                        .setBody(json)
                        .addHeader("Content-Type", "application/json")
                        .execute()
                        .toCompletableFuture()
                        .thenApply(Response::getResponseBody)
                        .thenApply(response -> {
                            try {
                                Message receivedMessage = mapper.readValue(response, Message.class);
                                if (receivedMessage == null) {
                                    String errorMessage = String.format("A Message wasn't received from the server %s.", url);
                                    log.info(errorMessage);
                                    return Optional.empty();
                                }
                                return Optional.of(receivedMessage);
                            } catch (IOException e) {
                                String errorMessage = "The message received was not a Message.";
                                log.warn(errorMessage);
                                System.out.println(errorMessage);
                                return Optional.empty();
                            }
                        }));

                log.info(String.format("Made request to the server %d", i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return completableFutures;
    }
}
