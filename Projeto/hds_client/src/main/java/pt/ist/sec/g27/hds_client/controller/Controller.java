package pt.ist.sec.g27.hds_client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_client.HdsClientApplication;
import pt.ist.sec.g27.hds_client.RestClient;
import pt.ist.sec.g27.hds_client.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_client.exceptions.NotFoundException;
import pt.ist.sec.g27.hds_client.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client.model.*;
import pt.ist.sec.g27.hds_client.utils.Utils;

import java.util.List;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final RestClient restClient = new RestClient();

    private boolean[] ackList; //TODO check if can be two, and if it is needed

    @VerifyAndSign
    @PostMapping("/buyGood")
    public Object buyGood(@RequestBody Message message) throws Exception {
        User me = HdsClientApplication.getMe();
        int goodId = message.getBody().getGoodId();

        Good good = HdsClientApplication.getGood(goodId);

        if (good == null) {
            String errorMessage = String.format("The good with id %d does not exists.", goodId);
            log.info(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        good.incrWts();
        int wTs = good.getwTs();
        int numberOfNotaries = HdsClientApplication.getNumberOfNotaries();
        ackList = new boolean[numberOfNotaries];
        Body body = new Body(me.getId(), goodId, message, wTs);

        List<Message> receivedMessages = restClient.postToMultipleNotaries(HdsClientApplication.getNotaries(), "/transferGood", body, me.getPrivateKey());


        int receives = 0, invalidReceives = 0, yesReceives = 0, noReceives = 0;
        Body invalidBody = null, yesBody = null, noBody = null;
        for (Message receivedMessage : receivedMessages) {
            Body receivedBody = receivedMessage.getBody();

            if (receivedBody != null) {
                int notaryId = receivedBody.getSenderId();
                Notary notary = HdsClientApplication.getNotary(notaryId);
                if (Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage) && receivedBody.getwTs() == wTs) {
                    ackList[notaryId] = true;
                    receives++;

                    if (!receivedBody.getStatus().is2xxSuccessful()) {
                        invalidReceives++;
                        invalidBody = receivedBody;
                    } else if (receivedBody.getResponse().equals("YES")) {
                        yesReceives++;
                        yesBody = receivedBody;
                    } else {
                        noReceives++;
                        noBody = receivedBody;
                    }

                    if (receives > (numberOfNotaries + HdsClientApplication.getByzantineFaultsLimit()) / 2) {
                        ackList = new boolean[numberOfNotaries];

                        if (yesReceives > noReceives && yesReceives > invalidReceives) {
                            String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                                            "user with the id %d, the response from the notary was %s",
                                    goodId,
                                    me.getId(),
                                    message.getBody().getSenderId(),
                                    yesBody.getResponse());

                            log.info(response);
                            System.out.println(response);
                            TransferCertificate transferCertificate = yesBody.getTransferCertificate();
                            HdsClientApplication.addTransferCertificate(transferCertificate);

                            return new Body(me.getId(), currentMessage);

                            // TODO ficamos aqui, alterar bodies para messages
                        } else if (noReceives > invalidReceives) {
                            log.info(noBody.getResponse());
                            System.out.println(noBody.getResponse());
                            return;
                        }
                        log.info(invalidBody.getResponse());
                        System.out.println(invalidBody.getResponse());
                        return;
                    }
                }
            }

            // TODO apagar desde aqui
            if (!isValidResponse(currentNotary, receivedBody))
                continue;

            if (!Utils.verifySingleMessage(currentNotary.getPublicKey(), receivedMessage)) {
                String errorMessage = "Could not verify the message.";
                log.info(errorMessage);
                continue;
            }

            if (currentMessage == null) {
                currentMessage = receivedMessage;
                notary = currentNotary;
            } else if (currentMessage.getBody().getTimestampInUTC().compareTo(receivedBody.getTimestampInUTC()) < 0) {
                currentMessage = receivedMessage;
                notary = currentNotary;
            }
        }

        if (currentMessage == null || currentMessage.getBody() == null) {
            String errorMessage = "There was no valid responses.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            throw new ResponseException(errorMessage);
        }

        Body receivedBody = currentMessage.getBody();

        String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                        "user with the id %d, the response from the notary was %s",
                goodId,
                me.getId(),
                message.getBody().getSenderId(),
                receivedBody.getResponse());

        log.info(response);
        System.out.println(response);

        TransferCertificate transferCertificate = receivedBody.getTransferCertificate();
        HdsClientApplication.addTransferCertificate(transferCertificate);

        notary.setTimestamp(receivedBody.getTimestamp());

        return new Body(me.getId(), currentMessage);
    }

    private boolean isValidResponse(Notary notary, Body body) {
        if (body == null) {
            String errorMessage = "The server could not respond.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return false;
        }

        if (!body.getStatus().is2xxSuccessful()) {
            log.info(body.getResponse());
            System.out.println(body.getResponse());
            return false;
        }

        if (body.getTimestampInUTC().compareTo(notary.getTimestampInUTC()) <= 0) {
            String errorMessage = "The message received is a repeat of a previous one.";
            log.info(errorMessage);
            System.out.println(errorMessage);
            return false;
        }
        return true;
    }
}
