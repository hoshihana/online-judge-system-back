package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.Account;
import pers.wjx.ojsb.pojo.enumeration.Role;

public interface AccountService {
    boolean existUsername(String username);

    boolean existEmail(String email);

    boolean existAccount(Integer id);

    boolean userRegister(String username, String password, String email);

    Role getRoleById(Integer id);

    Account authenticate(String username, String password);

    String getUsernameById(Integer id);

    boolean updateAvatar(Integer id, String avatar);

    boolean updateSchool(Integer id, String school);

    boolean updateProfile(Integer id, String profile);

    Account getAccountById(Integer id);
}
