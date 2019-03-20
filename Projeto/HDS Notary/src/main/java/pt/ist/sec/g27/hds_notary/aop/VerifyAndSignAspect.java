package pt.ist.sec.g27.hds_notary.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Aspect
@Component
public class VerifyAndSignAspect {

    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);

    @Around("@annotation(pt.ist.sec.g27.hds_notary.aop.VerifyAndSign)")
    public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        before();
        Object returnedValue = proceedingJoinPoint.proceed();
        return after(returnedValue);
    }

    private Object after(Object returnedValue) throws Exception {
        /*try {
            return SecurityUtils.sign(objectToByteArray(returnedValue));
        } catch (Exception e) {
            log.warn("Cannot sign the returned object.", e);
            throw e;
        }*/ // TODO remove the comments, not the code, when using the PT-CC, and remove the return intruction.
        return returnedValue;
    }

    private void before() throws Exception {
        /*boolean verified;
        try {
            verified = SecurityUtils.verify(#### update this ####);
        } catch (Exception e) {
            log.warn("Cannot verify the incoming message.", e);
            throw e;
        }
        if (!verified)
            throw new Exception("This message is not authentic.");*/ // TODO change exception
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
