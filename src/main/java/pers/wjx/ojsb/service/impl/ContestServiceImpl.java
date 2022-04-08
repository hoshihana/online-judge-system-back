package pers.wjx.ojsb.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pers.wjx.ojsb.pojo.Contest;
import pers.wjx.ojsb.pojo.enumeration.ContestType;
import pers.wjx.ojsb.repository.AccountRepository;
import pers.wjx.ojsb.repository.ContestProblemRepository;
import pers.wjx.ojsb.repository.ContestRepository;
import pers.wjx.ojsb.service.ContestService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;

@Service
public class ContestServiceImpl implements ContestService {

    @Value("${password-salt}")
    private String passwordSalt;

    @Resource
    private ContestRepository contestRepository;

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private ContestProblemRepository contestProblemRepository;

    @Override
    public Integer addContest(Integer authorId, String name, ContestType type, String description, String password, Date startTime, Date endTime) {
        Contest contest = new Contest();
        contest.setAuthorId(authorId);
        contest.setAuthorUsername(accountRepository.getUsernameById(authorId));
        contest.setName(name);
        contest.setType(type);
        contest.setDescription(description);
        contest.setPassword(SaSecureUtil.md5BySalt(password, passwordSalt));
        contest.setStartTime(startTime);
        contest.setEndTime(endTime);
        contestRepository.addContest(contest);
        return contest.getId();
    }

    @Override
    public boolean updateContestDetail(Integer id, String name, ContestType type, String description, String password) {
        return contestRepository.updateContestDetail(id, name, type, description, SaSecureUtil.md5BySalt(password, passwordSalt));
    }

    @Override
    public boolean setContestTime(Integer id, Date startTime, Date endTime) {
        return contestRepository.setContestTime(id, startTime, endTime);
    }

    @Override
    public boolean resetContest(Integer id, Date startTime, Date endTime) {
        // todo 清除所有参赛记录和提交记录，并重设开始、结束时间
        return false;
    }

    @Override
    public boolean setContestEndTime(Integer id, Date endTime) {
        return contestRepository.setContestEndTime(id, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setContestProblems(Integer id, ArrayList<Integer> problemIds) {
        try {
            contestProblemRepository.deleteContestProblemsByContestId(id);
            boolean success = true;
            for (int i = 0; i < problemIds.size(); i++) {
                success = success && contestProblemRepository.addContestProblem(id, problemIds.get(i), i + 1);
            }
            return true;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean participateContest(Integer id, Integer userId, String nickname) {
        return false;
    }

    @Override
    public Contest getContestById(Integer id) {
        return contestRepository.getContestById(id);
    }
}
