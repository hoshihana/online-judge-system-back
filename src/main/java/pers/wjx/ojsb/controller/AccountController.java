package pers.wjx.ojsb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.wjx.ojsb.entity.Account;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AccountService accountService;

    @GetMapping("/get")
    public List<Account> getAccount() {
        return accountService.getAllAccount();
    }
}
