package pt.ist.sec.g27.hds_notary.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_notary.Exceptions.ForbiddenException;
import pt.ist.sec.g27.hds_notary.Exceptions.NotFoundException;
import pt.ist.sec.g27.hds_notary.aop.VerifyAndSign;
import pt.ist.sec.g27.hds_notary.model.*;

import java.io.IOException;
import java.security.SignedObject;
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
    public Body getStateOfGood(@RequestBody Message message) {
        final int goodId = message.getBody().getGoodId();
        return Arrays.stream(notary.getGoods())
                .filter(good -> good.getId() == goodId)
                .map(good -> new Body(good.getOwnerId(), good.getState()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(new ErrorModel("The id that you specify does not exist.")));
    }

    @VerifyAndSign
    @PostMapping("/intentionToSell")
    public Body intentionToSell(@RequestBody Message message) {
        Body body = message.getBody();

        int userId = body.getUserId();
        int goodId = body.getGoodId();

        log.info(String.format("The public key of the user %d was successfully obtained.", userId));

        Good good = notary.getGood(goodId);
        if (good.getOwnerId() != userId) {
            log.warn(String.format("The state of the good %d could not be changed by the user %d.", goodId, userId));
            Body exceptionBody = new Body();
            exceptionBody.setExceptionResponse(new ForbiddenException(new ErrorModel("The state of the good could not be changed.")));
            return exceptionBody;
        }

        log.info(String.format("The good %d is owned by the user %d", goodId, userId));

        good.setState(State.ON_SALE);
        return new Body("YES");
    }

    @VerifyAndSign
    @PostMapping("/transferGood")
    public Body transferGood(@RequestBody Message message) {

        Body sellerBody = message.getBody();
        Body buyerBody = sellerBody.getMessage().getBody();

        int buyerGoodId = buyerBody.getGoodId();
        int sellerGoodId = sellerBody.getGoodId();
        int buyerId = buyerBody.getUserId();
        int sellerId = sellerBody.getUserId();

        if(buyerGoodId != sellerGoodId)
            throw new ForbiddenException(
                    new ErrorModel(
                            "Seller good ID does not match buyers good ID!"
                    )
            );

        Good g = Arrays.stream(notary.getGoods())
                .filter(x -> x.getId() == buyerGoodId)
                .findFirst()
                .orElse(null);

        if (g == null)
            throw new NotFoundException(
                    new ErrorModel(
                            "Good not found!"
                    )
            );

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

        return new Body("Yes");
    }
}