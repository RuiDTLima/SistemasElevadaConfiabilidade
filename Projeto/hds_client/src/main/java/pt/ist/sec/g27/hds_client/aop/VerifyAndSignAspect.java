package pt.ist.sec.g27.hds_client.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Aspect
@Component
public class VerifyAndSignAspect {
    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);

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

    private void before(Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof Message))
            throw new ResponseException("The incoming message is not acceptable.");

        Message message = (Message) args[0];

        if (message == null || message.getBody() == null)
            throw new ResponseException("The incoming message does not follow the specification.");

        verifyMessageStructure(message.getBody());
        verifySignature(message);
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

    private void verifyMessageStructure(Body body) {
        int userId = body.getUserId();

        // TODO nunca é -1
        if (userId == -1 || body.getTimestamp() == null || HdsClientApplication.getUser(userId) == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }
    }

    private void verifySignature(Message message) {
        boolean verified;
        try {
            verified = Utils.verifySingleMessage(HdsClientApplication.getUser(message.getBody().getUserId()).getPublicKey(), message);
        } catch (Exception e) {
            String errorMessage = "Could not verify the signature of the message";
            log.warn(errorMessage);
            throw new UnverifiedException(errorMessage);
        }

        if (!verified)
            throw new UnverifiedException("This message is not authentic.");
    }
}
