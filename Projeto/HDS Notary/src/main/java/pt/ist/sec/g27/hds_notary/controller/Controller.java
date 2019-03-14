package pt.ist.sec.g27.hds_notary.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/getStateOfGood/{id}")
    public Good getStateOfGood(@PathVariable("id") int id) {
        return null;
    }
}
