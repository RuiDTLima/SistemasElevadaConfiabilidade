package pt.ist.sec.g27.hds_client_malicious.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_client_malicious.HdsClientMaliciousApplication;
import pt.ist.sec.g27.hds_client_malicious.RestClient;
import pt.ist.sec.g27.hds_client_malicious.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_client_malicious.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client_malicious.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client_malicious.model.Body;
import pt.ist.sec.g27.hds_client_malicious.model.Message;
import pt.ist.sec.g27.hds_client_malicious.model.TransferCertificate;
import pt.ist.sec.g27.hds_client_malicious.model.User;
import pt.ist.sec.g27.hds_client_malicious.utils.Utils;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final RestClient restClient = new RestClient();

    @VerifyAndSign
    @PostMapping("/buyGood")
    public Object buyGood(@RequestBody Message message) throws Exception {
        User notary = HdsClientMaliciousApplication.getNotary();
        User me = HdsClientMaliciousApplication.getMe();
        int goodId = message.getBody().getGoodId();

        Body body = new Body(me.getId(), goodId, message);
        Message receivedMessage;

        receivedMessage = restClient.post(HdsClientMaliciousApplication.getNotary(), "/transferGood", body, me.getPrivateKey());

        isValidResponse(notary, receivedMessage.getBody());

        if (!Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage)) {
            String errorMessage = "Could not verify the message.";
            log.info(errorMessage);
            throw new UnverifiedException(errorMessage);
        }

        Body receivedBody = receivedMessage.getBody();

        String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                        "user with the id %d, the response from the notary was %s",
                goodId,
                me.getId(),
                message.getBody().getUserId(),
                receivedBody.getResponse());

        log.info(response);
        System.out.println(response);

        TransferCertificate transferCertificate = receivedBody.getTransferCertificate();
        HdsClientMaliciousApplication.addTransferCertificate(transferCertificate);

        notary.setTimestamp(receivedBody.getTimestamp());

        return new Body(me.getId(), receivedMessage);
    }

    private void isValidResponse(User notary, Body body) {
        if (body == null) {
            String errorMessage = "The server could not respond.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            throw new ResponseException(errorMessage);
        }

        if (!body.getStatus().is2xxSuccessful()) {
            log.info(body.getResponse());
            System.out.println(body.getResponse());
            throw new ResponseException(body.getResponse());
        }

        if (body.getTimestampInUTC().compareTo(notary.getTimestampInUTC()) <= 0) {
            String errorMessage = "The message received is a repeat of a previous one.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            throw new ResponseException(errorMessage);
        }
    }
}