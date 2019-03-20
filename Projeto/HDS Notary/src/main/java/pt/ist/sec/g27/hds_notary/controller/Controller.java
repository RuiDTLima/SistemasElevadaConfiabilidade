package pt.ist.sec.g27.hds_notary.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_notary.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_notary.model.Body;
import pt.ist.sec.g27.hds_notary.model.GoodPair;
import pt.ist.sec.g27.hds_notary.model.Notary;
import pt.ist.sec.g27.hds_notary.model.State;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private final Notary notary;

    @Autowired
    public Controller(Notary notary) {
        this.notary = notary;
    }

    @VerifyAndSign
    @PostMapping("/getStateOfGood")
    public GoodPair getStateOfGood(@RequestBody Body body) {
        final int goodId = body.getGoodId();
        return Arrays.stream(notary.getGoods())
                .filter(good -> good.getId() == goodId)
                .findFirst()
                .map(good -> new GoodPair(good.getOwnerId(), good.getState()))
                .orElse(null);
    }

    @PostMapping("/intentionToSell/{id}")
    public String intentionToSell(@PathVariable("id") int goodId, HttpServletRequest request) {
        int userId = (int) request.getAttribute("id");

        try {
            notary.getUser(userId).getPublicKey();
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            log.warn(String.format("There was an error while trying to obtain the public key of the user %d.", userId));

            // TODO add error return;
        }

        log.info(String.format("The public key of the user %d was successfully obtained.", userId));

        // TODO validate request signature.

        notary.getGood(goodId).setState(State.ON_SALE);

        // TODO return correct response.
        return "ok";
    }

    @PostMapping("/transferGood/{id}")
    public String transferGood(@PathVariable("id") int goodId, HttpServletRequest request) {

        // TODO

        // Important:

        // Put the good' as NOT_ON_SALE
        // Change the ownership of the good
        // Receive a possible response of the Notary?

        // Less important:

        // Possible check Yes/No question before authorizing to sell
        // User can choose to transfer by mistake

        return "ok";

    }

}
