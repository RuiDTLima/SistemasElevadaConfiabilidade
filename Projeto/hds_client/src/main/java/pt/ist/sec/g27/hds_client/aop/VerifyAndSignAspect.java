package pt.ist.sec.g27.hds_client.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.ist.sec.g27.hds_client.HdsClientApplication;
import pt.ist.sec.g27.hds_client.exceptions.ConnectionException;
import pt.ist.sec.g27.hds_client.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.User;
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;
import pt.ist.sec.g27.hds_client.utils.Utils;

import java.security.PublicKey;

@Aspect
@Component
public class VerifyAndSignAspect {
    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public VerifyAndSignAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(pt.ist.sec.g27.hds_client.aop.VerifyAndSign)")
    public Object callHandler(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            before(proceedingJoinPoint.getArgs());
        } catch (UnverifiedException | ResponseException e) {
            log.info(e.getMessage(), e);
            System.out.println(e.getMessage());
            return after(new Body(e));
        }

        Object returnedValue;
        try {
            returnedValue = proceedingJoinPoint.proceed();
        } catch (UnverifiedException | ResponseException | ConnectionException e) {
            log.info(e.getMessage(), e);
            System.out.println(e.getMessage());
            returnedValue = new Body(e);
        } catch (Exception e) {
            String errorMessage = "There was an error while trying to handle the buyGood request.";
            log.warn(errorMessage, e);
            System.out.println(errorMessage);
            throw e;
        }

        return after(returnedValue);
    }

    private Object after(Object returnedValue) throws Exception {
        try {
            User me = HdsClientApplication.getMe();
            byte[] sign = SecurityUtils.sign(me.getPrivateKey(), Utils.jsonObjectToByteArray(returnedValue));
            return new Message((Body) returnedValue, sign);
        } catch (Exception e) {
            log.warn("Cannot sign the returned object.", e);
            throw e;
        }
    }

    private void before(Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof Message))
            throw new ResponseException("The incoming message is not acceptable.");

        Message message = (Message) args[0];

        if (message == null || message.getBody() == null)
            throw new ResponseException("The incoming message does not follow the specification.");

        verifyMessageStructure(message.getBody());
        verifySignature(message);
    }

    private void verifyMessageStructure(Body body) {
        int userId = body.getUserId();

        if (userId == -1 || body.getTimestamp() == null || HdsClientApplication.getUser(userId) == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }
    }

    private void verifySignature(Message message) {
        boolean verified = verifyAllMessages(message);
        if (!verified)
            throw new UnverifiedException("This message is not authentic.");
    }

    private boolean verifyAllMessages(Message message) {
        if (message == null)    // It is known that in the first iteration the message is not null.
            return true;

        Body body = message.getBody();

        int userId = body.getUserId();
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
            jsonBody = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[]", e);
            throw new UnverifiedException("Something went wrong while verifying the signature.");
        }

        boolean verified;
        try {
            verified = SecurityUtils.verify(publicKey, jsonBody, message.getSignature());
        } catch (Exception e) {
            log.warn("Cannot verify the incoming message.", e);
            throw new UnverifiedException("Something went wrong while verifying the signature.");
        }

        return verified && verifyAllMessages(body.getMessage());
    }
}
