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
    public Body getStateOfGood(@RequestBody Body body) {
        final int goodId = body.getGoodId();
        return Arrays.stream(notary.getGoods())
                .filter(good -> good.getId() == goodId)
                .findFirst()
                .map(good -> new Body(good.getOwnerId(), good.getState()))
                .orElse(null);
    }

    @VerifyAndSign
    @PostMapping("/intentionToSell")
    public Body intentionToSell(@RequestBody Body body) {
        int userId = body.getBuyerId();
        int goodId = body.getGoodId();

        log.info(String.format("The public key of the user %d was successfully obtained.", userId));

        Good good = notary.getGood(goodId);// .setState(State.ON_SALE);
        if (good.getOwnerId() != userId) {
            log.warn(String.format("The state of the good %d could not be changed by the user %d.", goodId, userId));
            throw new ForbiddenException(new ErrorModel("The state of the good could not be changed."));
        }

        log.info(String.format("The good %d is owned by the user %d", goodId, userId));

        good.setState(State.ON_SALE);
        return new Body("YES");
    }

    @VerifyAndSign
    @PostMapping("/transferGood")
    public Body transferGood(@RequestBody Body body) {

        Good g = Arrays.stream(notary.getGoods())
                .filter(x -> x.getId() == body.getGoodId())
                .findFirst()
                .orElse(null);

        if(g == null)
            throw new NotFoundException(
                    new ErrorModel(
                            "Good not found!"
                    )
            );

        // Check if owner id coincides
        if(g.getOwnerId() == body.getOwnerId())
            throw new ForbiddenException(
                    new ErrorModel(
                        "Good does not belong to seller!"
                    )
            );

        if(g.getState() != State.ON_SALE)
            return new Body("No");

        g.setState(State.NOT_ON_SALE);
        g.setOwnerId(body.getBuyerId());

        return new Body("Yes");

    }

}
