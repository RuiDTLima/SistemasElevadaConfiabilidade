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
import pt.ist.sec.g27.hds_notary.Exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.SecurityUtils;
import pt.ist.sec.g27.hds_notary.Utils;
import pt.ist.sec.g27.hds_notary.model.*;

import java.security.PrivateKey;
import java.security.PublicKey;

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
        before(proceedingJoinPoint.getArgs());
        Object returnedValue = proceedingJoinPoint.proceed();
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

    private void before(Object[] args) throws Exception {
        // TODO Check exceptions
        if (args == null || args.length == 0 || !(args[0] instanceof Message))
            throw new Exception("The incoming message is not acceptable.");
        boolean verified = verifyAllMessages((Message) args[0]);
        if (!verified)
            throw new UnauthorizedException(new ErrorModel("This message is not authentic."));
    }

    private boolean verifyAllMessages(Message message) {
        // TODO check exceptions
        if (message == null)
            return true;

        Body body = message.getBody();
        if (body == null)
            return false;

        int userId = body.getUserId();
        User user = notary.getUser(userId);
        if (user == null) {
            log.info("User does not exist.");
            throw new UnauthorizedException(new ErrorModel("The user with id " + userId + " does not exist."));
        }
        PublicKey publicKey;
        try {
            publicKey = user.getPublicKey();
        } catch (Exception e) {
            log.info("Cannot find/load the public key of one user");
            throw new UnauthorizedException(new ErrorModel("Cannot find/load the public key of the user"));
        }
        byte[] jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.info("An error occurred while trying to convert object to string", e);
            throw new UnauthorizedException(new ErrorModel("Something went wrong while verifying the signature."));
        }
        boolean verified;
        try {
            verified = SecurityUtils.verify(publicKey, jsonBody, message.getSignature());
        } catch (Exception e) {
            log.info("Cannot verify the incoming message.", e);
            throw new UnauthorizedException(new ErrorModel("Something went wrong while verifying the signature."));
        }
        return verified && verifyAllMessages(body.getMessage());
    }
}
