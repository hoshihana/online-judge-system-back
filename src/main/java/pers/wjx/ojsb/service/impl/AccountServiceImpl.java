package pers.wjx.ojsb.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Account;
import pers.wjx.ojsb.pojo.enumeration.Role;
import pers.wjx.ojsb.repository.AccountRepository;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;

@Service
public class AccountServiceImpl implements AccountService {

    @Value("${password-salt}")
    private String passwordSalt;

    @Resource
    private AccountRepository accountRepository;

    @Override
    public boolean existUsername(String username) {
        return accountRepository.getAccountByUsername(username) != null;
    }

    @Override
    public boolean existEmail(String email) {
        return accountRepository.getAccountByEmail(email) != null;
    }

    @Override
    public boolean existAccount(Integer id) {
        return accountRepository.countAccountsById(id) > 0;
    }

    @Override
    public boolean userRegister(String username, String password, String email) {
        Account userAccount = new Account();
        userAccount.setUsername(username);
        userAccount.setPassword(SaSecureUtil.md5BySalt(password, passwordSalt));
        userAccount.setEmail(email);
        userAccount.setRole(Role.USER);
        return accountRepository.addAccount(userAccount);
    }

    @Override
    public Role getRoleById(Integer id) {
        return accountRepository.getAccountById(id).getRole();
    }

    // 认证成功返回账户id，否则返回null
    @Override
    public Integer authenticate(String username, String password) {
        Account account = accountRepository.getAccountByUsername(username);
        if(account == null || account.getPassword().compareTo(SaSecureUtil.md5BySalt(password, passwordSalt)) != 0) {
            return null;
        } else {
            return account.getId();
        }
    }

    @Override
    public String getUsernameById(Integer id) {
        return accountRepository.getUsernameById(id);
    }
}
