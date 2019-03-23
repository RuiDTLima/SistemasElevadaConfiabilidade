package pt.ist.sec.g27.hds_notary.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.ist.sec.g27.hds_notary.Exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.SecurityUtils;
import pt.ist.sec.g27.hds_notary.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PublicKey;

@Aspect
@Component
public class VerifyAndSignAspect {

    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);

    private final Notary notary;

    @Autowired
    public VerifyAndSignAspect(Notary notary) {
        this.notary = notary;
    }

    @Around("@annotation(pt.ist.sec.g27.hds_notary.aop.VerifyAndSign)")
    public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        before(proceedingJoinPoint.getArgs());
        Object returnedValue = proceedingJoinPoint.proceed();
        return after(returnedValue);
    }

    private Object after(Object returnedValue) throws Exception {
        /*try {
            byte[] sign = SecurityUtils.sign(objectToByteArray(returnedValue));
            return new Message((Body) returnedValue, sign);
        } catch (Exception e) {
            log.warn("Cannot sign the returned object.", e);
            throw e;
        }*/ // TODO remove the comments, not the code, when using the PT-CC, and remove the return instruction.
        return returnedValue;
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
        if (message == null) return true;
        Body body = message.getBody();
        if (body == null) return false;
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
        boolean verified;
        try {
            verified = SecurityUtils.verify(publicKey, objectToByteArray(body), message.getSignature());
        } catch (Exception e) {
            log.info("Cannot verify the incoming message.", e);
            throw new UnauthorizedException(new ErrorModel("Something went wrong while verifying the signature"));
        }
        return verified && verifyAllMessages(body.getMessage());
    }

    private static byte[] objectToByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            log.warn("Your object needs to implement the interface Serializable. Your object is: " + object.toString(), e);
            throw e;
        }
    }
}
