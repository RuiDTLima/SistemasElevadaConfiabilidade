package pt.ist.sec.g27.hds_client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_client.HdsClientApplication;
import pt.ist.sec.g27.hds_client.RestClient;
import pt.ist.sec.g27.hds_client.exceptions.ConnectionException;
import pt.ist.sec.g27.hds_client.exceptions.ResponseException;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.model.*;

import java.util.stream.Stream;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private static final RestClient restClient = new RestClient();

    @PostMapping("/buyGood")
    public Body buyGood(@RequestBody Message message) {
        User me = HdsClientApplication.getMe();
        int goodId = message.getBody().getGoodId();

        Stream<Good> goods = HdsClientApplication.getMyGoods();

        if (goods.noneMatch(good -> good.getId() == goodId))
            return new Body("NO");

        Body body = new Body(me.getId(), message);
        Message receivedMessage;

        try {
            receivedMessage = restClient.post(me, "/transferGood", body, me.getPrivateKey());

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

            if (body.getTimestampInUTC().compareTo(HdsClientApplication.getNotary().getTimestampInUTC()) <= 0) {
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

            return new Body(receivedMessage);    //  TODO check if the return can be of the receivedBody?
        } catch (UnverifiedException | ResponseException | ConnectionException e) {
            log.info(e.getMessage(), e);
            System.out.println(e.getMessage());
        } catch (Exception e) {
            String errorMessage = "There was an error while trying to handle the buyGood request.";
            log.warn(errorMessage, e);
            System.out.println(errorMessage);
        }

        return new Body("NO");
    }
}
