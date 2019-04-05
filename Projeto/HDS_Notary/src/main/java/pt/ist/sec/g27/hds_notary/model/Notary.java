package pt.ist.sec.g27.hds_notary.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;

public class Notary {
    private User[] users;
    private Good[] goods;

    @JsonProperty("transfer-certificate")
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
        return Arrays.stream(users).filter(user -> user.getId() == userId).findFirst().orElse(null);
    }

    public Good getGood(int goodId) {
        return Arrays.stream(goods).filter(good -> good.getId() == goodId).findFirst().orElse(null);
    }

    public void addTransferCertificate(TransferCertificate transferCertificate) {
        transferCertificates.add(transferCertificate);
    }
}
