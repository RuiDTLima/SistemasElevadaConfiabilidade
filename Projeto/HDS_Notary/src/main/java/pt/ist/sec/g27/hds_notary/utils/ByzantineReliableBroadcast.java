package pt.ist.sec.g27.hds_notary.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.sec.g27.hds_notary.HdsNotaryApplication;
import pt.ist.sec.g27.hds_notary.model.Body;
import pt.ist.sec.g27.hds_notary.model.Message;
import pt.ist.sec.g27.hds_notary.model.Notary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ByzantineReliableBroadcast {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DELIVER_URL = "/deliver";
    private static final String ECHO_URL = "/echo";
    private static final String READY_URL = "/ready";
    private final static Logger log = LoggerFactory.getLogger(HdsNotaryApplication.class);

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

        while (true) {
            if (message != null && countReadys > (2 * HdsNotaryApplication.getByzantineFaultsLimit()) && !delivered) {
                delivered = true;
                return;
            }
        }
    }

    private void broadcast(Message message) {
        toAllNotaries(DELIVER_URL, message);
    }

    private void send(Message message) {
        if (message == null || message.getBody() == null) {
            // TODO exception
        }
        if (message.getBody().getSenderId() == HdsNotaryApplication.getMe().getId() && !sentEcho) {
            sentEcho = true;
            toAllNotaries(ECHO_URL, message);
        }
    }

    private void echo(Message message) {
        if (message == null || message.getBody() == null) {
            // TODO exception
        }
        if (echos[message.getBody().getSenderId()] == null) {
            echos[message.getBody().getSenderId()] = message;
            countEchos++;
        }

        if (message != null && countEchos > ((HdsNotaryApplication.getTotalNotaries() + HdsNotaryApplication.getByzantineFaultsLimit()) / 2) && !sentReady) {
            sentReady = true;
            toAllNotaries(READY_URL, message);
        }
    }

    private void ready(Message message) {
        if (message == null || message.getBody() == null) {
            // TODO exception
        }
        if (readys[message.getBody().getSenderId()] == null) {
            readys[message.getBody().getSenderId()] = message;
            countReadys++;
        }

        if (message != null && countReadys > HdsNotaryApplication.getByzantineFaultsLimit() && !sentReady) {
            sentReady = true;
            toAllNotaries(READY_URL, message);
        }
    }

    private void toAllNotaries(String uri, Message message) {
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
    }
}
