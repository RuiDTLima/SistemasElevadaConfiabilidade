package pt.ist.sec.g27.hds_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;
import pt.ist.sec.g27.hds_client.utils.Utils;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.User;

import java.security.PrivateKey;

public class RestClient {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String server = "http://localhost:";
    private final RestTemplate rest;
    private final HttpHeaders headers;

    public RestClient() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public Body post(User user, String uri, Body body, PrivateKey privateKey) throws Exception {
        byte[] jsonBody = mapper.writeValueAsBytes(body);
        Message message = new Message(body, SecurityUtils.sign(privateKey, jsonBody));
        String json = mapper.writeValueAsString(message);
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + user.getPort() + uri, HttpMethod.POST, requestEntity, String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful())
            return null;

        Message receivedMessage = mapper.readValue(responseEntity.getBody(), Message.class);
        boolean verify = SecurityUtils.verify(user.getPublicKey(), Utils.jsonObjectToByteArray(receivedMessage.getBody()), receivedMessage.getSignature());

        if (!verify)
            throw new UnverifiedException("The response received did not originate from the notary.");

        return receivedMessage.getBody();
    }
}
