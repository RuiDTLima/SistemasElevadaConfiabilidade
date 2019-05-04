package pt.ist.sec.g27.hds_client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.Arrays;

public class AppState {
    private Notary[] notaries;
    private User[] users;
    private Good[] goods;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ArrayList<TransferCertificate> transferCertificates;

    public Notary[] getNotaries() {
        return notaries;
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

    public Notary getNotary(int notaryId) {
        return Arrays.stream(notaries)
                .filter(notary -> notary.getId() == notaryId)
                .findFirst()
                .orElse(null);
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

    public void addTransferCertificate(TransferCertificate transferCertificate) {
        if (transferCertificates == null)
            transferCertificates = new ArrayList<>();
        transferCertificates.add(transferCertificate);
    }
}
