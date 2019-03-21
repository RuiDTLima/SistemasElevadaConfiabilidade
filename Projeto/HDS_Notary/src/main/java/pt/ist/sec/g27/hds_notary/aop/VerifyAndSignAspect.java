package pt.ist.sec.g27.hds_notary.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pt.ist.sec.g27.hds_notary.SecurityUtils;
import pt.ist.sec.g27.hds_notary.model.Message;
import pt.ist.sec.g27.hds_notary.model.Notary;

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
        }*/ // TODO remove the comments, not the code, when using the PT-CC, and remove the return intruction.
        return returnedValue;
    }

    private void before(Object[] args) throws Exception {
        /*if (args == null || args.length == 0 || !(args[0] instanceof Message))
            throw new Exception("The incoming message is not acceptable.");*/ // TODO change exception
        /*boolean verified = verifyAllMessages((Message) args[0]);
        if (!verified)
            throw new Exception("This message is not authentic.");*/ // TODO change exception
    }

    private boolean verifyAllMessages(Message message) {
        if (message == null) return true;
        int userId = message.getBody().getOwnerId();
        boolean verified;
        try {
            PublicKey publicKey = notary.getUser(userId).getPublicKey();
            verified = SecurityUtils.verify(publicKey, objectToByteArray(message.getBody()), message.getSignature());
        } catch (Exception e) {
            log.warn("Cannot verify the incoming message.", e);
            return false;
        }
        return verified && verifyAllMessages(message.getBody().getMessage());
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
