package pt.ist.sec.g27.hds_notary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.sec.g27.hds_notary.model.GoodPair;
import pt.ist.sec.g27.hds_notary.model.Notary;

import java.util.Arrays;

@RestController
public class Controller {

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
}
