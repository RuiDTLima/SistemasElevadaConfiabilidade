package pt.ist.sec.g27.hds_notary.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import pt.ist.sec.g27.hds_notary.exceptions.HttpExceptions;
import pt.ist.sec.g27.hds_notary.exceptions.NotFoundException;
import pt.ist.sec.g27.hds_notary.exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.model.*;
import pt.ist.sec.g27.hds_notary.utils.ProofOfWork;
import pt.ist.sec.g27.hds_notary.utils.SecurityUtils;
import pt.ist.sec.g27.hds_notary.utils.Utils;

import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

@Aspect
@Component
public class VerifyAndSignAspect {
    private final static Logger log = LoggerFactory.getLogger(VerifyAndSignAspect.class);
    private final static String WITH_PT_CC = "withPTCC";

    private final Environment env;
    private final AppState appState;
    private final ObjectMapper objectMapper;
    private final int notaryId;

    @Autowired
    public VerifyAndSignAspect(Environment env, AppState appState, ObjectMapper objectMapper, int notaryId) {
        this.env = env;
        this.appState = appState;
        this.objectMapper = objectMapper;
        this.notaryId = notaryId;
    }

    @Around("@annotation(pt.ist.sec.g27.hds_notary.aop.VerifyAndSign)")
    public Object callHandler(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        VerifyAndSign verifyAndSign = method.getAnnotation(VerifyAndSign.class);
        boolean verifyInnerNotary = verifyAndSign.verifyInnerNotary();
        boolean verifyOuterNotary = verifyAndSign.verifyOuterNotary();
        try {
            log.info("Processing before method.");
            before(proceedingJoinPoint.getArgs(), verifyInnerNotary, verifyOuterNotary);
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
        String keyValue = env.getProperty(WITH_PT_CC);
        if (Boolean.parseBoolean(keyValue)) {
            try {
                byte[] sign = SecurityUtils.sign(Utils.jsonObjectToByteArray(returnedValue));
                return new Message((Body) returnedValue, sign);
            } catch (Exception e) {
                log.warn("Cannot sign the returned object.", e);
                throw e;
            }
        }

        // This code is used when the signing is done without CC.
        Notary me = appState.getNotary(notaryId);
        PrivateKey privateKey = SecurityUtils.readPrivate(me.getPrivateKeyPath());
        byte[] sign = SecurityUtils.sign(privateKey, Utils.jsonObjectToByteArray(returnedValue));
        return new Message((Body) returnedValue, sign);
    }

    private void before(Object[] args, boolean verifyInnerNotary, boolean verifyOuterNotary) throws JsonProcessingException, NoSuchAlgorithmException {
        if (args == null || args.length == 0 || !(args[0] instanceof Message)) {
            String errorMessage = "The incoming message is not acceptable.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, -1, -1);
        }

        Message message = (Message) args[0];

        if (message == null || message.getBody() == null) {
            String errorMessage = "The incoming message does not follow the specification.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, -1);
        }

        if (!verifyOuterNotary) {
            log.info("Verifying message's proof of work.");
            verifyProofOfWork(message);
        }
        log.info("Verifying message structure.");
        verifyOuterMessageStructure(message, verifyOuterNotary);
        log.info("Verifying inner message structure.");
        verifyInnerMessageStructure(message.getBody().getMessage(), verifyInnerNotary);
        log.info("Verifying message signature.");
        verifySignature(message, verifyInnerNotary, verifyOuterNotary);
    }

    private void verifyProofOfWork(Message message) throws JsonProcessingException, NoSuchAlgorithmException {
        if (message.getProofOfWork() == null || !ProofOfWork.verify(message.getBody(), message.getProofOfWork())) {
            String errorMessage = "The proof of work received was not correct.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());
        }
    }

    private void verifyOuterMessageStructure(Message message, boolean verifyOuterNotary) {
        Body body = message.getBody();
        int senderId = body.getSenderId();

        if (senderId == -1 || (verifyOuterNotary && appState.getNotary(senderId) == null) || (!verifyOuterNotary && appState.getUser(senderId) == null)) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());
        }
    }

    private void verifyInnerMessageStructure(Message innerMessage, boolean verifyInnerNotary) {
        if (innerMessage == null)
            return;

        Body innerBody = innerMessage.getBody();
        if (innerBody == null) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, -1, -1);
        }
        int senderId = innerBody.getSenderId();
        if (senderId == -1) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, -1, -1);
        }
        if ((verifyInnerNotary && appState.getNotary(senderId) == null) || (!verifyInnerNotary && appState.getUser(senderId) == null)) {
            String errorMessage = "The message structure specification was not followed.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, -1, -1);
        }
    }

    private void verifySignature(Message message, boolean verifyInnerNotary, boolean verifyOuterNotary) {
        boolean verified = verifyOuterSignature(message, verifyOuterNotary) && verifyInnerSignature(message.getBody().getMessage(), verifyInnerNotary);
        if (!verified) {
            String errorMessage = "This message is not authentic.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, message.getBody().getrId(), message.getBody().getwTs());
        }
    }

    private boolean verifyOuterSignature(Message message, boolean verifyOuterNotary) {
        Body body = message.getBody();
        return verify(body, getPublicKey(body, verifyOuterNotary), message.getSignature());
    }

    private boolean verifyInnerSignature(Message message, boolean verifyInnerNotary) {
        if (message == null)
            return true;
        Body body = message.getBody();
        return verify(body, getPublicKey(body, verifyInnerNotary), message.getSignature());
    }

    private PublicKey getPublicKey(Body body, boolean isNotary) {
        int senderId = body.getSenderId();
        if (isNotary) {
            Notary notary = appState.getNotary(senderId);
            if (notary == null) {
                String errorMessage = String.format("The notary with id %d does not exist.", senderId);
                log.info(errorMessage);
                throw new UnauthorizedException(errorMessage, body.getrId(), body.getwTs());
            }
            try {
                return notary.getPublicKey();
            } catch (Exception e) {
                String errorMessage = "Cannot find/load the public key of one notary.";
                log.info(errorMessage);
                throw new UnauthorizedException(errorMessage, body.getrId(), body.getwTs());
            }
        }
        return getPublicKeyFromUser(body);
    }

    private PublicKey getPublicKeyFromUser(Body body) {
        int senderId = body.getSenderId();
        User user = appState.getUser(senderId);
        if (user == null) {
            String errorMessage = String.format("The user with id %d does not exist.", senderId);
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, body.getrId(), body.getwTs());
        }
        try {
            return user.getPublicKey();
        } catch (Exception e) {
            String errorMessage = "Cannot find/load the public key of one user.";
            log.info(errorMessage);
            throw new UnauthorizedException(errorMessage, body.getrId(), body.getwTs());
        }
    }

    private boolean verify(Body body, PublicKey publicKey, byte[] signature) {
        byte[] jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.warn("An error occurred while trying to convert object to byte[].", e);
            throw new UnauthorizedException("Something went wrong while verifying the signature.", body.getrId(), body.getwTs());
        }

        try {
            return SecurityUtils.verify(publicKey, jsonBody, signature);
        } catch (Exception e) {
            log.warn("Cannot verify the incoming message.", e);
            throw new UnauthorizedException("Something went wrong while verifying the signature.", body.getrId(), body.getwTs());
        }
    }
}
