package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.entity.Account;

import java.util.List;

@Repository
@Mapper
public interface AccountRepository {

    List<Account> getAllAccount();
}
