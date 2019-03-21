package pt.ist.sec.g27.hds_notary.model;

import java.util.Arrays;

public class Notary {
    private User[] users;
    private Good[] goods;

    public User[] getUsers() {
        return users;
    }

    public Good[] getGoods() {
        return goods;
    }

    public User getUser(int userId) {
        return Arrays.stream(users).filter(user -> user.getId() == userId).findFirst().orElse(null);
    }

    public Good getGood(int goodId) {
        return Arrays.stream(goods).filter(good -> good.getId() == goodId).findFirst().orElse(null);
    }
}
