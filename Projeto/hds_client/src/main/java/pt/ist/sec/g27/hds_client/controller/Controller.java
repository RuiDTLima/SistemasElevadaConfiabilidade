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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final RestClient restClient = new RestClient();
    private static final String YES = "YES";
    private static final String NO = "NO";
    private static final String BUY_GOOD_URL = "/buyGood";
    private static final String TRANSFER_GOOD_URL = "/transferGood";

    private boolean[] ackList; //TODO check if can be two, in hds client application and here, and if it is needed

    @VerifyAndSign
    @PostMapping(BUY_GOOD_URL)
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
        Good signedGood = new Good(goodId, buyerBody.getSenderId(), good.getName(), State.NOT_ON_SALE, wTs, me.getId());
        byte[] sigma = SecurityUtils.sign(me.getPrivateKey(), Utils.jsonObjectToByteArray(signedGood));
        Body body = new Body(me.getId(), goodId, message, wTs, sigma);

        List<Message> receivedMessages = restClient.postToMultipleNotaries(HdsClientApplication.getNotaries(), TRANSFER_GOOD_URL, body, me.getPrivateKey());

        int receives = 0, invalidReceives = 0, yesReceives = 0, noReceives = 0;
        Message invalidMessage = null, yesMessage = null, noMessage = null;
        List<Integer> receivedwTs = new ArrayList<>();
        for (Message receivedMessage : receivedMessages) {
            Body receivedBody = receivedMessage.getBody();

            if (receivedBody != null) {
                int notaryId = receivedBody.getSenderId();
                Notary notary = HdsClientApplication.getNotary(notaryId);
                if (notary != null && Utils.verifySingleMessage(notary.getPublicKey(), receivedMessage)) {
                    int currentwTs = receivedBody.getwTs();
                    receivedwTs.add(currentwTs);
                    if (currentwTs == -1 && !receivedBody.getStatus().is2xxSuccessful()) {
                        ackList[notaryId] = true;
                        receives++;
                        invalidReceives++;
                        invalidMessage = receivedMessage;
                        if (isEnoughResponses(numberOfNotaries, receives)) {
                            ackList = new boolean[numberOfNotaries];
                            return invalidResponse(me, invalidMessage);
                        }
                    }
                    if (currentwTs == wTs) {
                        ackList[notaryId] = true;
                        receives++;

                        if (!receivedBody.getStatus().is2xxSuccessful()) {
                            invalidReceives++;
                            invalidMessage = receivedMessage;
                        } else if (receivedBody.getResponse().equals(YES)) {
                            yesReceives++;
                            yesMessage = receivedMessage;
                        } else {
                            noReceives++;
                            noMessage = receivedMessage;
                        }

                        if (isEnoughResponses(numberOfNotaries, receives)) {
                            ackList = new boolean[numberOfNotaries];
                            if (yesReceives > noReceives && yesReceives > invalidReceives) {
                                return yesResponse(message.getBody().getSenderId(), me, goodId, yesMessage);
                            } else if (noReceives > invalidReceives) {
                                return noResponse(message.getBody().getSenderId(), me, goodId, noMessage);
                            }
                            return invalidResponse(me, invalidMessage);
                        }
                    }
                }
            }
        }

        if (isEnoughResponses(numberOfNotaries, receivedwTs.size())) {
            int max = Collections.max(receivedwTs);
            good.setwTs(max);
            return buyGood(message);
        }

        String errorMessage = "There was no valid responses.";
        log.info(errorMessage);
        System.out.println(errorMessage);
        throw new ResponseException(errorMessage);
    }

    private Object yesResponse(int senderId, User me, int goodId, Message yesMessage) {
        Body yesBody = yesMessage.getBody();
        String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                        "user with the id %d, the response from the notary was %s",
                goodId,
                me.getId(),
                senderId,
                yesBody.getResponse());

        log.info(response);
        System.out.println(response);
        TransferCertificate transferCertificate = yesBody.getTransferCertificate();
        HdsClientApplication.addTransferCertificate(transferCertificate);
        return new Body(me.getId(), YES, yesMessage);
    }

    private Object noResponse(int senderId, User me, int goodId, Message noMessage) {
        Body noBody = noMessage.getBody();
        String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                        "user with the id %d, the response from the notary was %s",
                goodId,
                me.getId(),
                senderId,
                noBody.getResponse());
        log.info(response);
        System.out.println(response);
        return new Body(me.getId(), NO, noMessage);
    }

    private Object invalidResponse(User me, Message invalidMessage) {
        Body invalidBody = invalidMessage.getBody();
        log.info(invalidBody.getResponse());
        System.out.println(invalidBody.getResponse());
        return new Body(me.getId(), invalidBody.getResponse(), invalidMessage);
    }

    private boolean isEnoughResponses(int numberOfNotaries, int receives) {
        return receives > (numberOfNotaries + HdsClientApplication.getByzantineFaultsLimit()) / 2;
    }
}
