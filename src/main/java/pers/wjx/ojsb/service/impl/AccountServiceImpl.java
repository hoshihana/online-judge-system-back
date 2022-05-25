package pers.wjx.ojsb.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Account;
import pers.wjx.ojsb.pojo.Participation;
import pers.wjx.ojsb.pojo.enumeration.Role;
import pers.wjx.ojsb.repository.*;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;

@Service
public class AccountServiceImpl implements AccountService {

    @Value("${password-salt}")
    private String passwordSalt;

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private ProblemRepository problemRepository;

    @Resource
    private ContestRepository contestRepository;

    @Resource
    private ParticipationRepository participationRepository;

    @Resource
    private ProblemUserRepository problemUserRepository;

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
        userAccount.setAvatar(username);
        userAccount.setProfile("这个人很懒，什么都没有写~");
        return accountRepository.addAccount(userAccount);
    }

    @Override
    public Role getRoleById(Integer id) {
        return accountRepository.getAccountById(id).getRole();
    }

    // 认证成功返回账户id，否则返回null
    @Override
    public Account authenticate(String username, String password) {
        Account account = accountRepository.getAccountByUsername(username);
        if(account == null || account.getPassword().compareTo(SaSecureUtil.md5BySalt(password, passwordSalt)) != 0) {
            return null;
        } else {
            return account;
        }
    }

    @Override
    public boolean checkPassword(Integer id, String password) {
        Account account = accountRepository.getAccountById(id);
        return account != null && account.getPassword().compareTo(SaSecureUtil.md5BySalt(password, passwordSalt)) == 0;
    }

    @Override
    public boolean updatePassword(Integer id, String password) {
        return accountRepository.setPassword(id, SaSecureUtil.md5BySalt(password, passwordSalt));
    }

    @Override
    public String getUsernameById(Integer id) {
        return accountRepository.getUsernameById(id);
    }

    @Override
    public boolean updateAvatar(Integer id, String avatar) {
        return accountRepository.setAvatar(id, avatar);
    }

    @Override
    public boolean updateSchool(Integer id, String school) {
        return accountRepository.setSchool(id, school);
    }

    @Override
    public boolean updateProfile(Integer id, String profile) {
        return accountRepository.setProfile(id, profile);
    }

    @Override
    public Account getAccountById(Integer id) {
        Account account = accountRepository.getAccountById(id);
        if(account == null) {
            return null;
        }
        account.setTriedProblemAmount(problemUserRepository.countTriedProblemByUserId(id));
        account.setPassedProblemAmount(problemUserRepository.countPassedProblemByUserId(id));
        if(account.getRole() == Role.USER) {
            account.setParticipatedContestAmount(participationRepository.countParticipatedContestByUserId(id));
        }
        if(account.getRole() == Role.ADMIN) {
            account.setOwnedProblemAmount(problemRepository.countProblemByAuthorId(id));
            account.setOwnedContestAmount(contestRepository.countContestByAuthorId(id));
        }
        return account;
    }
}
