package pt.ist.sec.g27.hds_notary.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_notary.Exceptions.ForbiddenException;
import pt.ist.sec.g27.hds_notary.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_notary.model.*;

import javax.servlet.http.HttpServletRequest;
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

    @VerifyAndSign
    @PostMapping("/intentionToSell")
    public String intentionToSell(@RequestBody Body body) {
        int userId = body.getUserId();
        int goodId = body.getGoodId();

        log.info(String.format("The public key of the user %d was successfully obtained.", userId));

        Good good = notary.getGood(goodId);// .setState(State.ON_SALE);
        if (good.getOwnerId() != userId) {
            log.warn(String.format("The state of the good %d could not be changed by the user %d.", goodId, userId));
            throw new ForbiddenException(new ErrorModel("The state of the good could not be changed."));
        }

        log.info(String.format("The good %d is owned by the user %d", goodId, userId));

        good.setState(State.ON_SALE);
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
