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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private final ObjectMapper mapper;
    private final AppState appState;
    private final int notaryId;

    @Autowired
    public Controller(ObjectMapper objectMapper, AppState appState, int notaryId) {
        this.mapper = objectMapper;
        this.appState = appState;
        this.notaryId = notaryId;
    }

    @VerifyAndSign
    @PostMapping("/getStateOfGood")
    public Object getStateOfGood(@RequestBody Message message) {
        Body body = message.getBody();
        final int goodId = body.getGoodId();
        log.info(String.format("Obtaining the state of good with id %d", goodId));

        Good good = appState.getGood(goodId);

        int rId = body.getrId();

        if (good == null)
            throw new NotFoundException("The id that you specify does not exist.", rId, -1);

        log.info(String.format("The good with id %d belongs to the user with id %d", goodId, good.getOwnerId()));

        return new Body(notaryId, good.getOwnerId(), good.getState(), body.getrId(), good.getwTs());
    }

    @VerifyAndSign
    @PostMapping("/intentionToSell")
    public Object intentionToSell(@RequestBody Message message) {
        Body body = message.getBody();

        int userId = body.getSenderId();

        int goodId = body.getGoodId();

        log.info(String.format("The public key of the user %d was successfully obtained.", userId));

        Good good = appState.getGood(goodId);

        int wTs = body.getwTs();

        if (good == null) {
            String errorMessage = String.format("The good with id %d does not exist.", goodId);
            log.info(errorMessage);
            throw new NotFoundException(errorMessage, -1, wTs);
        }

        if (good.getOwnerId() != userId) {
            log.info(String.format("The state of the good %d could not be changed by the user %d.", goodId, userId));
            throw new ForbiddenException("You do not have that good.", -1, wTs);
        }

        if (good.getState().equals(State.ON_SALE)) {
            String errorMessage = "The good is already on sale.";
            log.info(errorMessage);
            return new Body(notaryId, "NO", -1, wTs);
        }

        if (wTs > good.getwTs()) {
            good.setState(State.ON_SALE);
            log.info(String.format("The good with id %d owned by the user with id %d is now on sale.", goodId, userId));
            saveState();
            return new Body(notaryId, "YES", -1, wTs);
        }
        return new Body(notaryId, "NO", -1, wTs);
    }

    @VerifyAndSign
    @PostMapping("/transferGood")
    public Object transferGood(@RequestBody Message message) {
        Body sellerBody = message.getBody();
        Body buyerBody = sellerBody.getMessage().getBody();

        int buyerGoodId = buyerBody.getGoodId();
        int sellerGoodId = sellerBody.getGoodId();
        int buyerId = buyerBody.getSenderId();
        int sellerId = sellerBody.getSenderId();

        User buyer = appState.getUser(buyerId);
        User seller = appState.getUser(sellerId);

        buyer.setTimestamp(buyerBody.getTimestamp());
        seller.setTimestamp(sellerBody.getTimestamp());

        if (buyerId == sellerId) {
            String errorMessage = "The user cannot buy from itself.";
            log.info(errorMessage);
            throw new ForbiddenException(errorMessage);
        }

        if (buyerGoodId != sellerGoodId) {
            String errorMessage = "The good id sent from the seller does not match the good id sent from the buyer.";
            log.info(errorMessage);
            throw new ForbiddenException(errorMessage);
        }

        Good g = appState.getGood(buyerGoodId);

        if (g == null) {
            String errorMessage = "Good not found.";
            log.info(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        // Check if owner id coincides
        if (g.getOwnerId() != sellerId) {
            String errorMessage = String.format("Good with id %d does not belong to the seller.", buyerGoodId);
            log.info(errorMessage);
            throw new ForbiddenException(errorMessage);
        }

        if (g.getState() != State.ON_SALE) {
            String errorMessage = String.format("The good with id %d is not on sale.", buyerGoodId);
            log.info(errorMessage);
            return new Body(notaryId, "No");
        }

        g.setState(State.NOT_ON_SALE);
        g.setOwnerId(buyerId);

        TransferCertificate transferCertificate = new TransferCertificate(buyerId, sellerId, buyerGoodId);
        appState.addTransferCertificate(transferCertificate);

        log.info(String.format("The good with id %d was transferred from the user with id %d to the user with id %d.", buyerGoodId, sellerId, buyerId));

        saveState();

        return new Body(notaryId, "Yes", transferCertificate);
    }

    private void saveState() {
        // Writing to a backup file
        try (FileOutputStream fileOutputStream = new FileOutputStream(HdsNotaryApplication.BACKUP_STATE_PATH)) {
            mapper.writeValue(fileOutputStream, appState);
            log.info("The backup state has been updated.");
        } catch (IOException e) {
            log.error("There was an error while trying to save the backup state.", e);
            return;
        }
        // Writing to current state.
        try {
            Files.copy(Paths.get(HdsNotaryApplication.BACKUP_STATE_PATH), Paths.get(HdsNotaryApplication.STATE_PATH), StandardCopyOption.REPLACE_EXISTING);
            log.info("The current state has been updated.");
        } catch (IOException e) {
            log.error("There was an error while trying to copy the backup state to the current state.");
        }
    }
}