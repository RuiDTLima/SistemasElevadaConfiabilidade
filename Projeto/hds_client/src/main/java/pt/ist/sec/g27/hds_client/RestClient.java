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

        if (receivedMessage == null) {
            String errorMessage = "A message wasn't received.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }

        return receivedMessage;
    }
}
