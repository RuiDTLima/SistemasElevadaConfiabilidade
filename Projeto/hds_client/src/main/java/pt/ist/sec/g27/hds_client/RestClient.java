package pt.ist.sec.g27.hds_client;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.User;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;

import java.security.PrivateKey;

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
        String url = server + user.getPort() + uri;
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
        if (receivedMessage == null || receivedMessage.getBody() == null) {
            log.info(String.format("The received message was null when making a request to %s.", url));
            throw new ResponseException("Did not received a message.");
        }

        verifyTimestamp(receivedMessage, receivedMessage.getBody().getMessage());

        if (!SecurityUtils.verifyAllMessages(receivedMessage))
            throw new UnverifiedException("The response received did not originate from the notary.");

        Body receivedBody = receivedMessage.getBody();
        if (!receivedBody.getStatus().is2xxSuccessful())
            throw new ResponseException(receivedBody.getResponse());

        return receivedMessage;
    }

    private void verifyTimestamp(Message message, Message innerMessage) {
        if (innerMessage == null) {
            Body body = message.getBody();
            if (body.getTimestamp() == null) { // TODO está mal pq o cliente não manda timestamp para outro cliente no caso de haver excecao
                String errorMessage = "The message structure specification was not followed.";
                log.info(errorMessage);
                throw new ResponseException(errorMessage);
            }
            if (body.getTimestampInUTC().compareTo(HdsClientApplication.getNotary().getTimestampInUTC()) <= 0) {
                String errorMessage = "The response received was duplicated.";
                log.info(errorMessage);
                throw new ResponseException(errorMessage);
            }
            return;
        }
        if (innerMessage.getBody() == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }
        verifyTimestamp(innerMessage, innerMessage.getBody().getMessage());
    }
}
