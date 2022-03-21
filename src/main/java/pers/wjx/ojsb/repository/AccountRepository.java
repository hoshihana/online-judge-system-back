package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Account;

@Repository
@Mapper
public interface AccountRepository {

    Account getAccountByUsername(String username);

    Account getAccountByEmail(String username);

    Account getAccountById(Integer id);

    boolean addAccount(Account account);
}
