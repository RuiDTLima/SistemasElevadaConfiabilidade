package pt.ist.sec.g27.hds_notary.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.ist.sec.g27.hds_notary.exceptions.HttpExceptions;
import pt.ist.sec.g27.hds_notary.exceptions.NotFoundException;
import pt.ist.sec.g27.hds_notary.exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.model.AppState;
import pt.ist.sec.g27.hds_notary.model.Body;
import pt.ist.sec.g27.hds_notary.model.Message;
import pt.ist.sec.g27.hds_notary.model.User;
import pt.ist.sec.g27.hds_notary.utils.SecurityUtils;
import pt.ist.sec.g27.hds_notary.utils.Utils;

import java.security.PrivateKey;
import java.security.PublicKey;

@Aspect
@Component
public class VerifyAndSignAspect {
    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);

    private final AppState appState;
    private final ObjectMapper objectMapper;
    private final int notaryId;

    @Autowired
    public VerifyAndSignAspect(AppState appState, ObjectMapper objectMapper, int notaryId) {
        this.appState = appState;
        this.objectMapper = objectMapper;
        this.notaryId = notaryId;
    }

    @Around("@annotation(pt.ist.sec.g27.hds_notary.aop.VerifyAndSign)")
    public Object callHandler(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            log.info("Processing before method.");
            before(proceedingJoinPoint.getArgs());
        } catch (HttpExceptions e) {
            log.info(String.format("Returning before exception from notary with id %d", notaryId));
            return after(new Body(notaryId, e));
        }

        Object returnedValue;
        try {
            log.info("Processing proceed method.");
            returnedValue = proceedingJoinPoint.proceed();
        } catch (HttpExceptions e) {
            returnedValue = new Body(notaryId, e);
        }

        log.info("Processing after method.");
        return after(returnedValue);
    }

    private Object after(Object returnedValue) throws Exception {
        /*try {
            byte[] sign = SecurityUtils.sign(Utils.jsonObjectToByteArray(returnedValue));
            return new Message((Body) returnedValue, sign);
        } catch (Exception e) {
            log.warn("Cannot sign the returned object.", e);
            throw e;
        }*/
        // This code is used when the signing is done without CC.
        PrivateKey privateKey = SecurityUtils.readPrivate("keys/notary.key");
        byte[] sign = SecurityUtils.sign(privateKey, Utils.jsonObjectToByteArray(returnedValue));
        return new Message((Body) returnedValue, sign);
    }

    private void before(Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof Message)) {
            String errorMessage = "The incoming message is not acceptable.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, -1, -1);
        }

        Message message = (Message) args[0];

        if (message == null || message.getBody() == null) {
            String errorMessage = "The incoming message does not follow the specification.";
            throw new NotFoundException(errorMessage, -1, -1);
        }

        log.info("Verifying message structure.");
        verifyMessageStructure(message);
        log.info("Verifying message signature.");
        verifySignature(message);
    }

    private void verifyMessageStructure(Message message) {
        if (message == null)    // It is known that in the first iteration the message is not null.
            return;

        Body body = message.getBody();
        int userId = body.getSenderId();

        if (userId == -1 || appState.getUser(userId) == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());  // TODO changed
        }
        verifyMessageStructure(body.getMessage());
    }

    private void verifySignature(Message message) {
        boolean verified = verifyAllMessages(message);
        if (!verified) {
            String errorMessage = "This message is not authentic.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());  // TODO changed
        }
    }

    private boolean verifyAllMessages(Message message) {
        if (message == null)    // It is known that in the first iteration the message is not null.
            return true;

        Body body = message.getBody();

        int userId = body.getSenderId();
        User user = appState.getUser(userId);

        if (user == null) {
            String errorMessage = String.format("The user with id %d does not exist.", userId);
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());  // TODO changed
        }

        PublicKey publicKey;
        try {
            publicKey = user.getPublicKey();
        } catch (Exception e) {
            String errorMessage = "Cannot find/load the public key of one user";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());  // TODO changed
        }

        byte[] jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[].", e);
            throw new UnauthorizedException("Something went wrong while verifying the signature.",message.getBody().getrId(), message.getBody().getwTs());  // TODO changed
        }

        boolean verified;
        try {
            verified = SecurityUtils.verify(publicKey, jsonBody, message.getSignature());
        } catch (Exception e) {
            log.warn("Cannot verify the incoming message.", e);
            throw new UnauthorizedException("Something went wrong while verifying the signature.", message.getBody().getrId(), message.getBody().getwTs()); // TODO changed
        }

        return verified && verifyAllMessages(body.getMessage());
    }
}
