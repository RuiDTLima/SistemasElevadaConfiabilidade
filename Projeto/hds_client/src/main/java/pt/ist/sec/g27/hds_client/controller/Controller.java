package pt.ist.sec.g27.hds_client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_client.HdsClientApplication;
import pt.ist.sec.g27.hds_client.RestClient;
import pt.ist.sec.g27.hds_client.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_client.model.Body;
import pt.ist.sec.g27.hds_client.model.Message;
import pt.ist.sec.g27.hds_client.model.TransferCertificate;
import pt.ist.sec.g27.hds_client.model.User;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final RestClient restClient = new RestClient();

    @VerifyAndSign
    @PostMapping("/buyGood")
    public Object buyGood(@RequestBody Message message) throws Exception {
        User me = HdsClientApplication.getMe();
        int goodId = message.getBody().getGoodId();

        Body body = new Body(me.getId(), goodId, message);
        Message receivedMessage;

        receivedMessage = restClient.post(HdsClientApplication.getNotary(), "/transferGood", body, me.getPrivateKey());

        Body receivedBody = receivedMessage.getBody();

        if (receivedBody == null) {
            String unsignedMessage = "The server could not respond.";
            log.info(unsignedMessage);
            System.out.println(unsignedMessage);
            return new Body("NO");
        }


        if (!receivedBody.getStatus().is2xxSuccessful()) {
            log.info(receivedBody.getResponse());
            System.out.println(receivedBody.getResponse());
            return new Body(receivedMessage);
        }

        if (receivedBody.getTimestampInUTC().compareTo(HdsClientApplication.getNotary().getTimestampInUTC()) <= 0) {
            String errorMessage = "The message received is a repeat of a previous one.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return new Body("NO");
        }

        String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                        "user with the id %d, the response from the notary was %s",
                goodId,
                me.getId(),
                message.getBody().getUserId(),
                receivedBody.getResponse());

        log.info(response);
        System.out.println(response);

        TransferCertificate transferCertificate = receivedBody.getTransferCertificate();
        HdsClientApplication.addTransferCertificate(transferCertificate);

        return new Body(me.getId(), goodId, receivedMessage);// TODO check, tem de mandar tmb o status?
    }
}
