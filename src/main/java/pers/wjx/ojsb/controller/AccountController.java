package pers.wjx.ojsb.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.wjx.ojsb.constraint.EmailConstraint;
import pers.wjx.ojsb.constraint.PasswordConstraint;
import pers.wjx.ojsb.constraint.UsernameConstraint;
import pers.wjx.ojsb.entity.Account;
import pers.wjx.ojsb.exception.AlreadyExistedException;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController {

    @Resource
    private AccountService accountService;

    @PostMapping("/register")
    public boolean accountRegister(@UsernameConstraint String username, @PasswordConstraint String password, @EmailConstraint String email) {
        if(accountService.existUsername(username)) {
            throw new AlreadyExistedException("该用户名已被注册");
        }
        if(accountService.existEmail(email)) {
            throw new AlreadyExistedException("该邮箱已被注册");
        }
        return accountService.addAccount(new Account(null, username, password, email));
    }
}
