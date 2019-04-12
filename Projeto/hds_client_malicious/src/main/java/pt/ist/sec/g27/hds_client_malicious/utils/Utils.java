package pt.ist.sec.g27.hds_client_malicious.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.sec.g27.hds_client_malicious.HdsClientMaliciousApplication;
import pt.ist.sec.g27.hds_client_malicious.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client_malicious.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client_malicious.model.Body;
import pt.ist.sec.g27.hds_client_malicious.model.Message;
import pt.ist.sec.g27.hds_client_malicious.model.User;

import java.security.PublicKey;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] jsonObjectToByteArray(Object object) throws JsonProcessingException {
        log.info("Converting object to byte[].");
        return objectMapper.writeValueAsBytes(object);
    }

    public static boolean verifySingleMessage(PublicKey notaryKey, Message receivedMessage) {
        byte[] jsonBody;

        try {
            jsonBody = jsonObjectToByteArray(receivedMessage.getBody());
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[]", e);
            throw new UnverifiedException("Something went wrong while verifying the signature.");
        }

        return SecurityUtils.verify(notaryKey, jsonBody, receivedMessage.getSignature());
    }

    public static void verifyAllMessages(Message receivedMessage, String url) {
        Body receivedBody = receivedMessage.getBody();
        if (receivedBody == null) {
            log.info(String.format("The received message was null when making a request to %s.", url));
            throw new ResponseException("Did not received a message.");
        }

        if (!receivedBody.getStatus().is2xxSuccessful())
            throw new ResponseException(receivedBody.getResponse());

        verifyInnerTimestamp(receivedMessage, receivedMessage.getBody().getMessage());

        if (!verifyAllSignatures(receivedMessage))
            throw new UnverifiedException("The response received did not originate from the expected source.");
    }

    private static void verifyTimestamp(Message message) {
        Body body = message.getBody();
        if (body.getTimestamp() == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }
        if (body.getTimestampInUTC().compareTo(HdsClientMaliciousApplication.getNotary().getTimestampInUTC()) <= 0) {
            String errorMessage = "The response received was duplicated.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }
    }

    private static void verifyInnerTimestamp(Message message, Message innerMessage) {
        if (innerMessage == null) {
            verifyTimestamp(message);
            return;
        }

        if (innerMessage.getBody() == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }
        verifyInnerTimestamp(innerMessage, innerMessage.getBody().getMessage());
    }

    private static boolean verifyAllSignatures(Message message) {
        if (message == null)    // It is known that in the first iteration the message is not null.
            return true;

        Body body = message.getBody();

        if (body == null) {
            String errorMessage = "The server could not respond correctly.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }

        int userId = body.getUserId();
        User user = HdsClientMaliciousApplication.getUser(userId);

        if (user == null) {
            String errorMessage = String.format("The user with id %d does not exist.", userId);
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }

        PublicKey publicKey;
        try {
            publicKey = user.getPublicKey();
        } catch (Exception e) {
            String errorMessage = "Cannot find/load the public key of one user";
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }

        byte[] jsonBody;
        try {
            jsonBody = jsonObjectToByteArray(body);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[]", e);
            throw new UnverifiedException("Something went wrong while verifying the signature.");
        }

        return SecurityUtils.verify(publicKey, jsonBody, message.getSignature()) && verifyAllSignatures(body.getMessage());
    }
}