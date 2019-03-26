package pt.ist.sec.g27.hds_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.User;

public class RestClient {
    private final String server = "http://localhost:";
    private final RestTemplate rest;
    private final HttpHeaders headers;
    private HttpStatus status;

    public RestClient() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public Body post(User user, String uri, Message message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(message);
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + user.getPort() + uri, HttpMethod.POST, requestEntity, String.class);

        Message receivedMessage = mapper.readValue(responseEntity.getBody(), Message.class);
        boolean verify = SecurityUtils.verify(user.getPublicKey(), Utils.objectToByteArray(receivedMessage.getBody()), receivedMessage.getSignature());
        this.setStatus(responseEntity.getStatusCode());
        if (!verify) return null;
        return receivedMessage.getBody();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
