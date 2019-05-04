package pt.ist.sec.g27.hds_client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.sec.g27.hds_client.HdsClientApplication;
import pt.ist.sec.g27.hds_client.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.Notary;
import pt.ist.sec.g27.hds_client.model.User;

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

        Message innerMessage = receivedBody.getMessage();
        if (innerMessage == null || innerMessage.getBody() == null){
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }

        verifyInnerTimestamp(receivedMessage, innerMessage);

        if (!verifyUserSignature(receivedMessage) || !verifyNotarySignature(innerMessage))
            throw new UnverifiedException("The response received did not originate from the expected source.");
    }

    private static boolean verifyNotarySignature(Message receivedMessage) {
        Body receivedBody = receivedMessage.getBody();
        int notaryId = receivedBody.getSenderId();
        Notary notary = HdsClientApplication.getNotary(notaryId);

        if (notary == null) {
            String errorMessage = String.format("The user with id %d does not exist.", notaryId);
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }

        PublicKey publicKey;
        try {
            publicKey = notary.getPublicKey();
        } catch (Exception e) {
            String errorMessage = "Cannot find/load the public key of one user";
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }

        byte[] jsonBody;
        try {
            jsonBody = jsonObjectToByteArray(receivedBody);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[]", e);
            throw new UnverifiedException("Something went wrong while verifying the signature.");
        }

        return SecurityUtils.verify(publicKey, jsonBody, receivedMessage.getSignature());
    }

    private static boolean verifyUserSignature(Message receivedMessage) {
        Body receivedBody = receivedMessage.getBody();
        int userId = receivedBody.getSenderId();
        User user = HdsClientApplication.getUser(userId);

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
            jsonBody = jsonObjectToByteArray(receivedBody);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[]", e);
            throw new UnverifiedException("Something went wrong while verifying the signature.");
        }

        return SecurityUtils.verify(publicKey, jsonBody, receivedMessage.getSignature());
    }

    private static void verifyTimestamp(Message message) {
        Body body = message.getBody();
        if (body.getTimestamp() == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new ResponseException(errorMessage);
        }
        if (body.getTimestampInUTC().compareTo(HdsClientApplication.getNotary(body.getSenderId()).getTimestampInUTC()) <= 0) {
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
}
