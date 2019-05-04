package pt.ist.sec.g27.hds_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pt.ist.sec.g27.hds_client.exceptions.ConnectionException;
import pt.ist.sec.g27.hds_client.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.Notary;
import pt.ist.sec.g27.hds_client.model.User;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RestClient {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private final String server = "http://localhost:";
    private final RestTemplate rest;
    private final HttpHeaders headers;

    public RestClient() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public Message post(User user, String uri, Body body, PrivateKey privateKey) throws Exception {
        String url = user.getUrl() + uri;
        return makeRequest(body, privateKey, url);
    }

    public List<Message> postToMultipleNotaries(Notary[] notaries, String uri, Body body, PrivateKey privateKey) throws Exception {
        List<Message> responses = new ArrayList<>();
        byte[] jsonBody = mapper.writeValueAsBytes(body);
        Message message = new Message(body, SecurityUtils.sign(privateKey, jsonBody));
        String json = mapper.writeValueAsString(message);

        List<CompletableFuture<Optional<Message>>> completableFutures = new ArrayList<>();
        try(AsyncHttpClient asyncClient = Dsl.asyncHttpClient()) {

            for (int i = 0; i < 7; i++) {
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
            for (CompletableFuture<Optional<Message>> cf : completableFutures) {
                Optional<Message> possibleMessage = cf.join();

                possibleMessage.ifPresent(responses::add);
            }
        }

        return responses;
    }

    private Message makeRequest(Body body, PrivateKey privateKey, String url) throws java.io.IOException {
        byte[] jsonBody = mapper.writeValueAsBytes(body);
        Message message = new Message(body, SecurityUtils.sign(privateKey, jsonBody));
        String json = mapper.writeValueAsString(message);
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

        ResponseEntity<String> responseEntity;

        try {
            responseEntity = rest.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (RestClientException e) {
            String errorMessage = "Something went wrong while trying to connect to the server.";
            log.warn(errorMessage);
            throw new ConnectionException(errorMessage);
        }

        Message receivedMessage = mapper.readValue(responseEntity.getBody(), Message.class);

        if (receivedMessage == null) {
            String errorMessage = "A message wasn't received.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }

        return receivedMessage;
    }
}
