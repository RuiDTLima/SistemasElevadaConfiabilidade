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
import pt.ist.sec.g27.hds_notary.exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.model.Body;
import pt.ist.sec.g27.hds_notary.model.Message;
import pt.ist.sec.g27.hds_notary.model.Notary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ByzantineReliableBroadcast {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DELIVER_URL = "/deliver";
    private static final String ECHO_URL = "/echo";
    private static final String READY_URL = "/ready";
    private final static Logger log = LoggerFactory.getLogger(ByzantineReliableBroadcast.class);
    private final static int TIMEOUT = 5000; // 5 seconds
    private final Object monitor = new Object();
    private Message clientMessage;

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
        clientMessage = message;

        broadcast(message);
    }

    private void broadcast(Message message) {
        log.info(String.format("Broadcast message from notary with id %d to all notaries", notaryId));
        toAllNotaries(DELIVER_URL, new Body(notaryId, message));
        if (!delivered) {
            synchronized (monitor) {
                long t = Timeouts.start(TIMEOUT);
                long remaining = Timeouts.remaining(t);
                while (true) {
                    try {
                        monitor.wait(remaining);
                    } catch (InterruptedException e) {
                        if (delivered) {
                            monitor.notify();
                        }
                        break;
                    }
                    remaining = Timeouts.remaining(t);
                    if (Timeouts.isTimeout(remaining)) {
                        break;
                    }
                }
            }
        }
        if (!delivered) {
            String errorMessage = "Message is not valid.";
            log.info(errorMessage);
            Body body = message.getBody();
            throw new UnauthorizedException(errorMessage, body.getrId(), body.getwTs());
        }
        log.info("Successfully broadcast the message.");
    }

    public void send(Message message) {
        log.info(String.format("Send method in notary with id %d", notaryId));
        if (message == null || message.getBody() == null) {
            String errorMessage = "The message or its body cannot be null.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }
        Body body = message.getBody();
        if (body.getSenderId() == notaryId && !sentEcho/* && clientMessage.equals(message)*/) {
            sentEcho = true;
            log.info("To all notaries echo request.");
            toAllNotaries(ECHO_URL, new Body(notaryId, body.getMessage()));
        }
    }

    public void echo(Message message) {
        log.info(String.format("Echo method in notary with id %d", notaryId));
        if (message == null || message.getBody() == null) {
            String errorMessage = "The message or its body cannot be null.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }
        int senderId = message.getBody().getSenderId();
        if (echos[senderId] == null) {
            log.info(String.format("Received echo from notary with id %d for the first time.", senderId));
            echos[senderId] = message;
            countEchos++;
        }
        if (countEchos > ((HdsNotaryApplication.getTotalNotaries() + HdsNotaryApplication.getByzantineFaultsLimit()) / 2) && !sentReady) {
            sentReady = true;
            log.info("To All Notaries ready request.");
            toAllNotaries(READY_URL, new Body(notaryId, message.getBody().getMessage()));
        }
    }

    public void ready(Message message) {
        log.info(String.format("Ready method in notary with id %d", notaryId));
        if (message == null || message.getBody() == null) {
            String errorMessage = "The message or its body cannot be null.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }
        int senderId = message.getBody().getSenderId();
        if (readys[senderId] == null) {
            log.info(String.format("Received ready from notary with id %d for the first time.", senderId));
            readys[senderId] = message;
            countReadys++;
        }
        if (countReadys > HdsNotaryApplication.getByzantineFaultsLimit() && !sentReady) {
            sentReady = true;
            toAllNotaries(READY_URL, new Body(notaryId, message.getBody().getMessage()));
        }
        if (countReadys > (2 * HdsNotaryApplication.getByzantineFaultsLimit()) && !delivered) {
            delivered = true;
            synchronized (monitor) {
                monitor.notify();
            }
        }
    }

    private void toAllNotaries(String uri, Body body) {
        Notary[] notaries = HdsNotaryApplication.getNotaries();
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

        List<CompletableFuture<Response>> completableFutures = new ArrayList<>();
        try (AsyncHttpClient asyncClient = Dsl.asyncHttpClient()) {
            for (int i = 0; i < notaries.length; i++) {
                String url = notaries[i].getUrl() + uri;
                completableFutures.add(asyncClient.preparePost(url)
                        .setBody(json)
                        .addHeader("Content-Type", "application/json")
                        .execute()
                        .toCompletableFuture());
                log.info(String.format("Made request to the server %d", i));
            }
            for (CompletableFuture<Response> cf : completableFutures)
                cf.join();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
