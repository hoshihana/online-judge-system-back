package pers.wjx.ojsb.service;

import pers.wjx.ojsb.entity.Account;

public interface AccountService {
    boolean existUsername(String username);

    boolean existEmail(String email);

    boolean addAccount(Account account);
}
