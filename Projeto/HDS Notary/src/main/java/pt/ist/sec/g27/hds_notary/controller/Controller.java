package pt.ist.sec.g27.hds_notary.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_notary.Exceptions.UnauthorizedException;
import pt.ist.sec.g27.hds_notary.model.ErrorModel;
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

    @GetMapping("/getStateOfGood/{id}")
    public GoodPair getStateOfGood(@PathVariable("id") int id) {
        GoodPair goodPair = Arrays.stream(notary.getGoods())
                .filter(good -> good.getId() == id)
                .findFirst()
                .map(good -> new GoodPair(good.getOwnerId(), good.getState()))
                .orElse(null);
        return goodPair;
    }

    @PostMapping("/intentionToSell/{id}")
    public String intentionToSell(@PathVariable("id") int goodId, HttpServletRequest request) {


        // log.info(String.format("The public key of the user %d was successfully obtained.", userId));

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
