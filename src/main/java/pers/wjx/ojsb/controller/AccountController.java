package pers.wjx.ojsb.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.wjx.ojsb.constraint.EmailConstraint;
import pers.wjx.ojsb.constraint.PasswordConstraint;
import pers.wjx.ojsb.constraint.UsernameConstraint;
import pers.wjx.ojsb.exception.AlreadyExistedException;
import pers.wjx.ojsb.exception.UnauthorizedException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.pojo.LoginStatus;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController {

    @Resource
    private AccountService accountService;

    @PostMapping("/register")
    public String register(@UsernameConstraint String username, @PasswordConstraint String password, @EmailConstraint String email) {
        if (accountService.existUsername(username)) {
            throw new AlreadyExistedException("该用户名已被注册");
        }
        if (accountService.existEmail(email)) {
            throw new AlreadyExistedException("该邮箱已被注册");
        }
        if (accountService.userRegister(username, password, email)) {
            return "注册成功";
        } else {
            throw new InternalServerErrorException("注册失败");
        }
    }

    @PostMapping("/login")
    public String login(@NotBlank(message = "用户名不能为空") String username, @NotBlank(message = "密码不能为空") String password) {
        Integer accountId = accountService.authenticate(username, password);
        if(accountId == null) {
            throw new UnauthorizedException("用户名或密码错误");
        } else {
            StpUtil.login(accountId);
            StpUtil.getSession(true).setAttribute("username", username);
            return "登录成功";
        }
    }

    @GetMapping("/login/status")
    public LoginStatus getLoginStatus() {
        LoginStatus loginStatus = new LoginStatus();
        if(!StpUtil.isLogin()) {
            loginStatus.setLogin(false);
        } else {
            loginStatus.setLogin(true);
            loginStatus.setUserid(StpUtil.getLoginIdAsInt());
            loginStatus.setUsername((String)StpUtil.getSession().getAttribute("username"));
        }
        return loginStatus;
    }

    @PostMapping("/logout")
    public String logout() {
        StpUtil.logout();
        return "登出成功";
    }

    @GetMapping("/username")
    public String getUsernameById(Integer id) {
        return accountService.getUsernameById(id);
    }
}
