package pt.ist.sec.g27.hds_notary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_notary.HdsNotaryApplication;
import pt.ist.sec.g27.hds_notary.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_notary.exceptions.ForbiddenException;
import pt.ist.sec.g27.hds_notary.exceptions.NotFoundException;
import pt.ist.sec.g27.hds_notary.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@RestController
public class Controller {
    private static final String STATE_PATH = "state.json";
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private final ObjectMapper mapper;
    private final Notary notary;

    @Autowired
    public Controller(ObjectMapper objectMapper, Notary notary) {
        this.mapper = objectMapper;
        this.notary = notary;
    }

    @VerifyAndSign
    @PostMapping("/getStateOfGood")
    public Object getStateOfGood(@RequestBody Message message) {
        final int goodId = message.getBody().getGoodId();
        return Arrays.stream(notary.getGoods())
                .filter(good -> good.getId() == goodId)
                .map(good -> new Body(good.getOwnerId(), good.getState()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(new ErrorModel("The id that you specify does not exist.")));
    }

    @VerifyAndSign
    @PostMapping("/intentionToSell")
    public Object intentionToSell(@RequestBody Message message) {
        Body body = message.getBody();

        int userId = body.getUserId();
        int goodId = body.getGoodId();

        log.info(String.format("The public key of the user %d was successfully obtained.", userId));

        Good good = notary.getGood(goodId);
        if (good.getOwnerId() != userId) {
            log.warn(String.format("The state of the good %d could not be changed by the user %d.", goodId, userId));
            throw new ForbiddenException(new ErrorModel("You do not have that good."));
        }

        log.info(String.format("The good %d is owned by the user %d", goodId, userId));

        good.setState(State.ON_SALE);

        saveState();

        return new Body("YES");
    }

    @VerifyAndSign
    @PostMapping("/transferGood")
    public Object transferGood(@RequestBody Message message) {
        Body sellerBody = message.getBody();
        Body buyerBody = sellerBody.getMessage().getBody();

        int buyerGoodId = buyerBody.getGoodId();
        int sellerGoodId = sellerBody.getGoodId();
        int buyerId = buyerBody.getUserId();
        int sellerId = sellerBody.getUserId();

        if (buyerGoodId != sellerGoodId)
            throw new ForbiddenException(
                    new ErrorModel(
                            "Seller good ID does not match buyers good ID!"
                    )
            );

        Good g = Arrays.stream(notary.getGoods())
                .filter(x -> x.getId() == buyerGoodId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        new ErrorModel(
                                "Good not found!"
                        )
                ));

        // Check if owner id coincides
        if (g.getOwnerId() != sellerId)
            throw new ForbiddenException(
                    new ErrorModel(
                            "Good does not belong to seller!"
                    )
            );

        if (g.getState() != State.ON_SALE)
            return new Body("No");

        g.setState(State.NOT_ON_SALE);
        g.setOwnerId(buyerId);

        saveState();

        return new Body("Yes");
    }

    private void saveState() {
        try {
            // Writing to a file
            ClassLoader classLoader = HdsNotaryApplication.class.getClassLoader();
            File file = new File(classLoader.getResource(STATE_PATH).getFile());
            mapper.writeValue(file, notary);
            log.info("The state has been updated.");
        } catch (IOException e) {
            log.error("There was an error while trying to save the state.", e);
        }
    }

    // TODO verify if count of body is ok

    private boolean verifyCount(Body body){
        int bodyCount = body.getCount();
        int userId = body.getUserId();

        User user = Arrays.stream(notary.getUsers())
                .filter(x -> x.getId() == userId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(new ErrorModel("The id that you specify does not exist.")));

        int userCount = user.getCount();

        // Case when the count on the body is lower than the one that the notary has
        if ( bodyCount < userCount ){ return false; } // Or throw some exception

        return true; // Or just continue running

    }

}