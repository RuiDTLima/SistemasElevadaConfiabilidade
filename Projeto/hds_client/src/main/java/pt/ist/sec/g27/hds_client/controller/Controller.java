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
import pt.ist.sec.g27.hds_client.utils.SecurityUtils;
import pt.ist.sec.g27.hds_client.utils.Utils;

import java.util.List;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final RestClient restClient = new RestClient();

    private boolean[] ackList; //TODO check if can be two, in hds client application and here, and if it is needed

    @VerifyAndSign
    @PostMapping("/buyGood")
    public Object buyGood(@RequestBody Message message) throws Exception {
        User me = HdsClientApplication.getMe();
        Body buyerBody = message.getBody();
        int goodId = buyerBody.getGoodId();

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
        byte[] sigma = SecurityUtils.sign(me.getPrivateKey(), Utils.jsonObjectToByteArray(new Good(goodId, buyerBody.getSenderId(), good.getName(), State.NOT_ON_SALE, wTs, me.getId())));
        Body body = new Body(me.getId(), goodId, message, wTs, sigma);

        List<Message> receivedMessages = restClient.postToMultipleNotaries(HdsClientApplication.getNotaries(), "/transferGood", body, me.getPrivateKey());

        int receives = 0, invalidReceives = 0, yesReceives = 0, noReceives = 0;
        Message invalidMessage = null, yesMessage = null, noMessage = null;
        for (Message receivedMessage : receivedMessages) {
            Body receivedBody = receivedMessage.getBody();

            if (receivedBody != null) {
                int notaryId = receivedBody.getSenderId();
                Notary notary = HdsClientApplication.getNotary(notaryId);
                if (notary != null && Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage) && receivedBody.getwTs() == wTs) {
                    ackList[notaryId] = true;
                    receives++;

                    if (!receivedBody.getStatus().is2xxSuccessful()) {
                        invalidReceives++;
                        invalidMessage = receivedMessage;
                    } else if (receivedBody.getResponse().equals("YES")) {
                        yesReceives++;
                        yesMessage = receivedMessage;
                    } else {
                        noReceives++;
                        noMessage = receivedMessage;
                    }

                    if (receives > (numberOfNotaries + HdsClientApplication.getByzantineFaultsLimit()) / 2) {
                        ackList = new boolean[numberOfNotaries];
                        if (yesReceives > noReceives && yesReceives > invalidReceives) {
                            Body yesBody = yesMessage.getBody();
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

                            return new Body(me.getId(), yesMessage);
                        } else if (noReceives > invalidReceives) {
                            Body noBody = noMessage.getBody();
                            String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                                            "user with the id %d, the response from the notary was %s",
                                    goodId,
                                    me.getId(),
                                    message.getBody().getSenderId(),
                                    noBody.getResponse());
                            log.info(response);
                            System.out.println(response);
                            return new Body(me.getId(), noMessage);
                        }
                        Body invalidBody = invalidMessage.getBody();
                        log.info(invalidBody.getResponse());
                        System.out.println(invalidBody.getResponse());
                        return new Body(me.getId(), invalidMessage);
                    }
                }
            }
        }
        String errorMessage = "There was no valid responses.";
        log.info(errorMessage);
        System.out.println(errorMessage);
        throw new ResponseException(errorMessage);
    }
}
