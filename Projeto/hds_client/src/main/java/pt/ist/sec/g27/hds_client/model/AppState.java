package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class AppState {
    private static final String EXCEPTION_MESSAGE = "There is an error with the state file. Must contain notary information with id 0.";

    private User[] users;
    private Good[] goods;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ArrayList<TransferCertificate> transferCertificates;

    public User[] getUsers() {
        return users;
    }

    public Good[] getGoods() {
        return goods;
    }

    public ArrayList<TransferCertificate> getTransferCertificates() {
        return transferCertificates;
    }

    public User getUser(int userId) {
        return Arrays.stream(users)
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElse(null);
    }

    public Good getGood(int goodId) {
        return Arrays.stream(goods)
                .filter(good -> good.getId() == goodId)
                .findFirst()
                .orElse(null);
    }

    public User getNotary() {
        return Arrays.stream(this.users)
                .filter(user -> user.getId() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(EXCEPTION_MESSAGE));
    }

    public Stream<Good> getUsersGood(int userId) {
        return Arrays.stream(goods)
                .filter(good -> good.getOwnerId() == userId);
    }

    public void addTransferCertificate(TransferCertificate transferCertificate) {
        transferCertificates.add(transferCertificate);
    }
}
