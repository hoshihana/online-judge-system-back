package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.entity.Account;
import pers.wjx.ojsb.repository.AccountRepository;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;

@Service
public class AccountServiceImpl implements AccountService {

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
    public boolean addAccount(Account account) {
        return accountRepository.addAccount(account);
    }
}
