package pt.ist.sec.g27.hds_notary.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
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
public class ReliableBroadcast {
    /*private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DELIVER_URL = "/deliver";

    private Boolean sentEcho;
    private Boolean sentReady;
    private Boolean delivered;
    private Message[] echos;
    private Message[] readys;
    private int notaryId;

    @Autowired
    public ReliableBroadcast(int notaryId) {
        this.notaryId = notaryId;
    }

    public void init() {
        sentEcho = false;
        sentReady = false;
        delivered = false;
        echos = new Message[HdsNotaryApplication.getTotalNotaries()];
        readys = new Message[HdsNotaryApplication.getTotalNotaries()];
    }

    private void broadcast(Message message) throws Exception {
        Notary[] requestingNotaries = HdsNotaryApplication.getRemainingNotaries();
        Body body = new Body(notaryId, message);
        byte[] jsonBody = mapper.writeValueAsBytes(body);
        Message toSendMessage = new Message(body, SecurityUtils.sign(SecurityUtils.readPrivate(HdsNotaryApplication.getMe().getPrivateKeyPath()), jsonBody));
        String json = mapper.writeValueAsString(toSendMessage);

        List<CompletableFuture<Optional<Message>>> completableFutures = new ArrayList<>();
        try (AsyncHttpClient asyncClient = Dsl.asyncHttpClient()) {

            for (int i = 0; i < requestingNotaries.length; i++) {
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
            for (CompletableFuture<Optional<Message>> cf : completableFutures) {
                Optional<Message> possibleMessage = cf.join();

                possibleMessage.ifPresent(responses::add);
            }
        }

        return responses;
    }*/
}
