package pers.wjx.ojsb.service;

public interface AccountService {
    boolean existUsername(String username);

    boolean existEmail(String email);

    boolean userRegister(String username, String password, String email);

    String getRoleById(Integer id);

    Integer authenticate(String username, String password);
}
