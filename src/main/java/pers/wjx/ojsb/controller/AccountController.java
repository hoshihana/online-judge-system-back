package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.exception.NotFoundException;
import pers.wjx.ojsb.exception.UnauthorizedException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.pojo.Account;
import pers.wjx.ojsb.pojo.LoginStatus;
import pers.wjx.ojsb.pojo.enumeration.Role;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    @Resource
    private AccountService accountService;

    @PostMapping("/register")
    public String register(@Pattern(regexp = "^\\w+$", message = "用户名只能含有数字、字母或下划线")
                           @Length(min = 6, max = 16, message = "用户名长度必须在6到16位之间")
                           @NotBlank(message = "用户名不能为空") String username,
                           @Pattern(regexp = "^[A-Za-z0-9]+$", message = "密码只能含有数字或字母")
                           @Length(min = 6, max = 16, message = "密码长度必须在6到16位之间")
                           @NotBlank(message = "密码不能为空") String password,
                           @Email(message = "邮箱格式不正确")
                           @NotBlank(message = "邮箱不能为空") String email) {
        if (accountService.existUsername(username)) {
            throw new BadRequestException("该用户名已被注册");
        }
        if (accountService.existEmail(email)) {
            throw new BadRequestException("该邮箱已被注册");
        }
        if (accountService.userRegister(username, password, email)) {
            return "注册成功";
        } else {
            throw new InternalServerErrorException("注册失败");
        }
    }

    @PostMapping("/login")
    public LoginStatus login(@NotBlank(message = "用户名不能为空") String username, @NotBlank(message = "密码不能为空") String password) {
        Account account = accountService.authenticate(username, password);
        if (account == null) {
            throw new UnauthorizedException("用户名或密码错误");
        } else {
            StpUtil.login(account.getId());
            StpUtil.getSession(true).setAttribute("username", account.getUsername());
            StpUtil.getSession(true).setAttribute("role", account.getRole());
            return new LoginStatus(true, account.getId(), account.getUsername(), account.getRole());
        }
    }

    @GetMapping("/loginStatus")
    public LoginStatus getLoginStatus() {
        LoginStatus loginStatus = new LoginStatus();
        if (!StpUtil.isLogin()) {
            loginStatus.setLogin(false);
        } else {
            loginStatus.setLogin(true);
            loginStatus.setUserid(StpUtil.getLoginIdAsInt());
            loginStatus.setUsername((String) StpUtil.getSession().getAttribute("username"));
            loginStatus.setRole((Role) StpUtil.getSession().getAttribute("role"));
        }
        return loginStatus;
    }

    @PostMapping("/logout")
    public String logout() {
        StpUtil.logout();
        return "登出成功";
    }

    @SaCheckLogin
    @PatchMapping("/password")
    public String updatePassword(@NotNull(message = "密码不能为空") String password,
                                 @Pattern(regexp = "^[A-Za-z0-9]+$", message = "密码只能含有数字或字母")
                                 @Length(min = 6, max = 16, message = "密码长度必须在6到16位之间")
                                 @NotBlank(message = "密码不能为空") String newPassword) {
        if (!accountService.checkPassword(StpUtil.getLoginIdAsInt(), password)) {
            throw new BadRequestException("原密码错误");
        }
        if(accountService.checkPassword(StpUtil.getLoginIdAsInt(), newPassword)) {
            throw new BadRequestException("新密码不能与原密码相同");
        }
        if(accountService.updatePassword(StpUtil.getLoginIdAsInt(), newPassword)) {
            return "密码修改成功";
        } else {
            throw new InternalServerErrorException("密码修改失败");
        }
    }

    @SaCheckLogin
    @PatchMapping("/avatar")
    public String updateAvatar(@Length(min = 1, max = 40, message = "头像生成信息长度要在1到40之间") String avatar) {
        if (accountService.updateAvatar(StpUtil.getLoginIdAsInt(), avatar)) {
            return "头像修改成功";
        } else {
            throw new InternalServerErrorException("头像修改失败");
        }
    }

    @SaCheckLogin
    @PatchMapping("/school")
    public String updateSchool(@Length(min = 0, max = 40, message = "学校名称长度不能超过40") String school) {
        if (accountService.updateSchool(StpUtil.getLoginIdAsInt(), school)) {
            return "学校修改成功";
        } else {
            throw new InternalServerErrorException("学校修改失败");
        }
    }

    @SaCheckLogin
    @PatchMapping("/profile")
    public String updateProfile(String profile) {
        if (accountService.updateProfile(StpUtil.getLoginIdAsInt(), profile)) {
            return "个人简介修改成功";
        } else {
            throw new InternalServerErrorException("个人简介修改失败");
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Integer id) {
        Account account = accountService.getAccountById(id);
        if (account == null) {
            throw new NotFoundException("账号不存在");
        }
        if (StpUtil.getLoginIdAsInt() != id) {
            account.setEmail(null);
        }
        return account;
    }
}
