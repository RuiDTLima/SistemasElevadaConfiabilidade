package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;

public class AppState {
    private Notary notary;
    private User[] users;
    private Good[] goods;

    @JsonProperty("transfer-certificates")
    private ArrayList<TransferCertificate> transferCertificates;

    public AppState() {
    }

    public Notary getNotary() {
        return notary;
    }

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
        return Arrays.stream(users).filter(user -> user.getId() == userId).findFirst().orElse(null);
    }

    public Good getGood(int goodId) {
        return Arrays.stream(goods).filter(good -> good.getId() == goodId).findFirst().orElse(null);
    }

    public void addTransferCertificate(TransferCertificate transferCertificate) {
        transferCertificates.add(transferCertificate);
    }
}
