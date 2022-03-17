package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.entity.Account;
import pers.wjx.ojsb.repository.AccountRepository;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountRepository accountRepository;

    @Override
    public List<Account> getAllAccount() {
        return accountRepository.getAllAccount();
    }
}
