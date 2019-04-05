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
import pt.ist.sec.g27.hds_notary.exceptions.ForbiddenException;
import pt.ist.sec.g27.hds_notary.exceptions.HttpExceptions;
import pt.ist.sec.g27.hds_notary.exceptions.NotFoundException;
import pt.ist.sec.g27.hds_notary.exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.model.*;
import pt.ist.sec.g27.hds_notary.utils.SecurityUtils;
import pt.ist.sec.g27.hds_notary.utils.Utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;

@Aspect
@Component
public class VerifyAndSignAspect {
    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);

    private final Notary notary;
    private final ObjectMapper objectMapper;

    @Autowired
    public VerifyAndSignAspect(Notary notary, ObjectMapper objectMapper) {
        this.notary = notary;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(pt.ist.sec.g27.hds_notary.aop.VerifyAndSign)")
    public Object callHandler(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            before(proceedingJoinPoint.getArgs());
        } catch (HttpExceptions e) {
            return after(new Body(e));
        }

        Object returnedValue;
        try {
            returnedValue = proceedingJoinPoint.proceed();
        } catch (HttpExceptions e) {
            returnedValue = new Body(e);
        }

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
        // TODO remove the code below after testing and use the above code to sign with PT-CC
        PrivateKey privateKey = SecurityUtils.readPrivate("notary.key");
        byte[] sign = SecurityUtils.sign(privateKey, Utils.jsonObjectToByteArray(returnedValue));
        return new Message((Body) returnedValue, sign);
    }

    private void before(Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof Message))
            throw new UnauthorizedException("The incoming message is not acceptable.");

        Message message = (Message) args[0];

        if (message == null || message.getBody() == null)
            throw new NotFoundException("The incoming message does not follow the specification.");

        verifyTimestamp(message);
        verifySignature(message);
    }

    private void verifyTimestamp(Message message) {
        if (message == null)    // It is known that in the first iteration the message is not null.
            return;

        Body body = message.getBody();
        int userId = body.getUserId();

        ZonedDateTime lastUserTimestamp = notary.getUser(userId).getTimestampInUTC();

        if (body.getTimestampInUTC().compareTo(lastUserTimestamp) <= 0) {
            String errorMessage = "The message received is out of time, it was sent before the last one.";
            log.info(errorMessage);
            throw new ForbiddenException(errorMessage);
        }

        verifyTimestamp(body.getMessage());
    }

    private void verifySignature(Message message) {
        // TODO Check exceptions
        boolean verified = verifyAllMessages(message);
        if (!verified)
            throw new UnauthorizedException("This message is not authentic.");
    }

    private boolean verifyAllMessages(Message message) {
        if (message == null)    // It is known that in the first iteration the message is not null.
            return true;

        Body body = message.getBody();

        int userId = body.getUserId();
        User user = notary.getUser(userId);

        if (user == null) {
            String errorMessage = String.format("The user with id %d does not exist.", userId);
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage);
        }

        PublicKey publicKey;
        try {
            publicKey = user.getPublicKey();
        } catch (Exception e) {
            String errorMessage = "Cannot find/load the public key of one user";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage);
        }

        byte[] jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to string", e);  //  TODO check exception message and when it occurs
            throw new UnauthorizedException("Something went wrong while verifying the signature.");
        }

        boolean verified;
        try {
            verified = SecurityUtils.verify(publicKey, jsonBody, message.getSignature());
        } catch (Exception e) {
            log.warn("Cannot verify the incoming message.", e);
            throw new UnauthorizedException("Something went wrong while verifying the signature.");
        }

        return verified && verifyAllMessages(body.getMessage());
    }
}
