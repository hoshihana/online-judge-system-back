package pers.wjx.ojsb.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.enumeration.ContestType;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.pojo.enumeration.Visibility;
import pers.wjx.ojsb.repository.*;
import pers.wjx.ojsb.service.ContestService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

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

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private ParticipationRepository participationRepository;

    @Override
    public Integer addContest(Integer authorId, String name, ContestType type, String description, Boolean passwordSet, String password, Date startTime, Date endTime) {
        Contest contest = new Contest();
        contest.setAuthorId(authorId);
        contest.setAuthorUsername(accountRepository.getUsernameById(authorId));
        contest.setName(name);
        contest.setType(type);
        contest.setDescription(description);
        contest.setPasswordSet(passwordSet);
        contest.setPassword(passwordSet ? SaSecureUtil.md5BySalt(password, passwordSalt) : null);
        contest.setStartTime(startTime);
        contest.setEndTime(endTime);
        contestRepository.addContest(contest);
        return contest.getId();
    }

    @Override
    public boolean updateContestDetail(Integer id, String name, ContestType type, String description, Boolean passwordSet, String password) {
        return contestRepository.updateContestDetail(id, name, type, description, passwordSet, passwordSet ? SaSecureUtil.md5BySalt(password, passwordSalt) : null);
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
    public boolean participateContest(Integer id, Integer userId, String nickname, String password) throws BadRequestException {
        Contest contest = contestRepository.getContestById(id);
        if (!contest.getPasswordSet() || contest.getPassword().equals(SaSecureUtil.md5BySalt(password, passwordSalt))) {
            try {
                participationRepository.addParticipation(id, userId, accountRepository.getUsernameById(userId), nickname);
                return true;
            } catch (Exception ex) {
                throw new BadRequestException("无法重复参加比赛");
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isContestParticipant(Integer id, Integer userId) {
        Participation participation = participationRepository.getParticipation(id, userId);
        return participation != null;
    }

    @Override
    public ArrayList<ProblemEntry> getContestProblemEntries(Integer id) {
        return contestProblemRepository.getContestProblemEntries(id);
    }

    @Override
    public Problem getContestProblem(Integer id, Integer problemNumber) {
        return contestProblemRepository.getContestProblem(id, problemNumber);
    }

    @Override
    public Contest getContestById(Integer id) {
        return contestRepository.getContestById(id);
    }

    @Override
    public ArrayList<Record> getContestRecords(Integer id, String problemNumber, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer pageIndex, Integer pageSize) {
        Integer problemNumberInt = null;
        if (Pattern.matches("^\\d{1,8}$", problemNumber)) {
            problemNumberInt = Integer.valueOf(problemNumber);
        }
        return recordRepository.getContestRecords(id, problemNumberInt, username, submitLanguage, judgeResult, orderBy, asc, (pageIndex - 1) * pageSize, pageSize);
    }


    @Override
    public Integer countContestRecords(Integer id, String problemNumber, String username, Language submitLanguage, JudgeResult judgeResult) {
        Integer problemNumberInt = null;
        if (Pattern.matches("^\\d{1,8}$", problemNumber)) {
            problemNumberInt = Integer.valueOf(problemNumber);
        }
        return recordRepository.countContestRecords(id, problemNumberInt, username, submitLanguage, judgeResult);
    }

    @Override
    public Record getContestRecord(Integer id, Integer recordId) {
        return recordRepository.getContestRecord(id, recordId);
    }
}
