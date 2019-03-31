package pt.ist.sec.g27.hds_client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_client.HdsClientApplication;
import pt.ist.sec.g27.hds_client.RestClient;
import pt.ist.sec.g27.hds_client.exceptions.UnverifiedException;
import pt.ist.sec.g27.hds_client.model.*;

import java.io.IOException;
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
            return new Body("No");

        Body body = new Body(me.getId(), message);
        Body receivedBody;

        try {
            receivedBody = restClient.post(me, "/transferGood", body, me.getPrivateKey());
            String response = String.format("When trying to transfer the good with id %d from the user with id %d to " +
                    "user with the id %d, the response from the notary was %s",
                    goodId,
                    me.getId(),
                    message.getBody().getUserId(),
                    receivedBody.getResponse());

            log.info(response);
            System.out.println(receivedBody.getMessage());
            return new Body(receivedBody.getResponse());    //  TODO check if the return can be of the receivedBody?
        } catch (UnverifiedException e) {
            log.info(e.getMessage(), e);
        } catch (IOException e) {
            log.info(e.getMessage(), e);
            System.out.println("Couldn't be proved that the response originated from the notary.");
        } catch (Exception e) {
            log.warn("There was an error while trying to obtain the user private key");
            System.out.println("Your private key could be successfully obtained.");
        }

        return new Body("No");
    }
}
