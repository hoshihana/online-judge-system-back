package pers.wjx.ojsb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.enumeration.*;
import pers.wjx.ojsb.repository.*;
import pers.wjx.ojsb.service.ContestService;
import pers.wjx.ojsb.service.RecordService;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private ProblemRepository problemRepository;

    @Resource
    private ContestProblemRepository contestProblemRepository;

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private ParticipationRepository participationRepository;

    @Resource
    private ContestProblemUserRepository contestProblemUserRepository;

    @Resource
    private RecordService recordService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Integer addContest(Integer authorId, String name, ContestType type, String description, Boolean passwordSet, String password, Date startTime, Date endTime) {
        Contest contest = new Contest();
        contest.setAuthorId(authorId);
        contest.setAuthorUsername(accountRepository.getUsernameById(authorId));
        contest.setName(name);
        contest.setType(type);
        contest.setDescription(description);
        contest.setPasswordSet(passwordSet);
        contest.setPassword(passwordSet ? password : null);
        contest.setStartTime(startTime);
        contest.setEndTime(endTime);
        contestRepository.addContest(contest);
        return contest.getId();
    }

    @Override
    public boolean updateContestDetail(Integer id, String name, ContestType type, String description, Boolean passwordSet, String password) {
        return contestRepository.updateContestDetail(id, name, type, description, passwordSet, passwordSet ? password : null);
    }

    @Override
    public boolean setContestTime(Integer id, Date startTime, Date endTime) {
        return contestRepository.setContestTime(id, startTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetContest(Integer id, Date startTime, Date endTime) {
        try {
            participationRepository.deleteParticipationByContestId(id);
            contestProblemUserRepository.deleteRelationsByContestId(id);
            recordRepository.deleteRecordsByContestId(id);      // todo 相关judge未删除
            contestProblemRepository.resetContestProblem(id);
            contestRepository.setContestTime(id, startTime, endTime);
            stringRedisTemplate.delete("rank:" + id);
            return true;
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setContestEndTime(Integer id, Date endTime) {
        stringRedisTemplate.delete("rank:" + id);
        return contestRepository.setContestEndTime(id, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setContestProblems(Integer id, ArrayList<Integer> problemIds) {
        try {
            contestProblemRepository.deleteContestProblemsByContestId(id);
            boolean success = contestRepository.setContestProblemAmount(id, problemIds.size());
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
    public boolean validateProblemIds(Integer authorId, ArrayList<Integer> problemIds) {
        Set<Integer> set = new HashSet<>();
        for (Integer problemId : problemIds) {
            if (set.contains(problemId)) {
                return false;
            } else {
                Problem problem = problemRepository.getProblemById(problemId);
                if (problem == null || !problem.getAuthorId().equals(authorId)) {
                    return false;
                } else {
                    set.add(problemId);
                }
            }
        }
        return true;
    }

    @Override
    public ArrayList<ContestProblemUserRelation> getContestProblemUserRelations(Integer id, Integer userId) {
        return contestProblemUserRepository.getRelationsByContestIdAndUserId(id, userId);
    }

    @Override
    public ContestProblemUserRelation getContestProblemUserRelation(Integer id, Integer problemNumber, Integer userId) {
        return contestProblemUserRepository.getRelation(id, problemNumber, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean participateContest(Integer id, Integer userId, String nickname, String password) throws BadRequestException {
        Contest contest = contestRepository.getContestById(id);
        ArrayList<ProblemEntry> contestProblemEntries = contestProblemRepository.getContestProblemEntries(id);
        if (!contest.getPasswordSet() || contest.getPassword().equals(password)) {
            try {
                participationRepository.addParticipation(id, userId, accountRepository.getUsernameById(userId), nickname);
                contestRepository.increaseContestParticipantAmount(userId);
                for(int i = 0; i < contestProblemEntries.size(); i++) {
                    contestProblemUserRepository.addRelation(id, contestProblemEntries.get(i).getId(), userId, i + 1);
                }
                return true;
            } catch (Exception ex) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
    public TryPassAmountPair getContestTryPassAmountPair(Integer id, Integer problemNumber) {
        return new TryPassAmountPair(contestProblemUserRepository.countTriedParticipant(id, problemNumber), contestProblemUserRepository.countPassedParticipant(id, problemNumber));
    }

    @Override
    public Contest getContestById(Integer id) {
        return contestRepository.getContestById(id);
    }

    @Override
    public ArrayList<Contest> getRecentContests(Integer dayLimit) {
        ArrayList<Contest> contests = new ArrayList<>();
        Date current = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        contests.addAll(contestRepository.getOngoingContests());
        for(int i = 0; i < dayLimit; i++) {
            contests.addAll(contestRepository.getComingContestsByDay(simpleDateFormat.format(current)));
            current.setTime(current.getTime() + 24 * 60 * 60 * 1000);
        }
        return contests;
    }

    @Override
    public ArrayList<Contest> getContestsByKey(String key, Integer pageIndex, Integer pageSize) {
        key = key.trim();
        ArrayList<Contest> contests = new ArrayList<>();
        if (Pattern.matches("^\\d{1,8}$", key)) {
            Contest contest = getContestById(Integer.valueOf(key));
            if (contest != null && !contest.getName().contains(key)) {
                contests.add(contest);
            }
        }
        contests.addAll(contestRepository.getContestsByName(key, (pageIndex - 1) * pageSize, pageSize));
        return contests;
    }

    @Override
    public Integer countContestsByKey(String key) {
        key = key.trim();
        int total = 0;
        if (Pattern.matches("^\\d{1,8}$", key)) {
            Contest contest = getContestById(Integer.valueOf(key));
            if (contest != null && !contest.getName().contains(key)) {
                total++;
            }
        }
        total += contestRepository.countContestsByName(key);
        return total;
    }

    @Override
    public ArrayList<Contest> getUserContestsByKey(Integer authorId, String key, Boolean showPractice, Boolean showCompetition, Boolean orderByStartTimeAsc, Integer pageIndex, Integer pageSize) {
        key = key.trim();
        ArrayList<Contest> contests = new ArrayList<>();
        ArrayList<ContestType> types = getContestTypes(showPractice, showCompetition);
        if (Pattern.matches("^\\d{1,8}$", key)) {
            Contest contest = getContestById(Integer.valueOf(key));
            if (contest != null && contest.getAuthorId().equals(authorId) && types.contains(contest.getType()) && !contest.getName().contains(key)) {
                contests.add(contest);
            }
        }
        contests.addAll(contestRepository.getUserContestsByName(authorId, key, types, orderByStartTimeAsc, (pageIndex - 1) * pageSize, pageSize));
        return contests;
    }

    @Override
    public Integer countUserContestsByKey(Integer authorId, String key, Boolean showPractice, Boolean showCompetition) {
        key = key.trim();
        int total = 0;
        ArrayList<ContestType> types = getContestTypes(showPractice, showCompetition);
        if (Pattern.matches("^\\d{1,8}$", key)) {
            Contest contest = getContestById(Integer.valueOf(key));
            if (contest != null && contest.getAuthorId().equals(authorId) && types.contains(contest.getType()) && !contest.getName().contains(key)) {
                total++;
            }
        }
        total += contestRepository.countUserContestsByName(authorId, key, types);
        return total;
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

    @Override
    public ArrayList<Record> getContestProblemRecentRecords(Integer id, Integer problemNumber, Integer userId, Integer limit) {
        return recordRepository.getContestRecentRecords(id, problemNumber, userId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer submitCode(Integer id, Integer problemNumber, Problem problem, Integer userId, Language submitLanguage, String code) {
        try {
            contestProblemRepository.increaseContestSubmit(id, problemNumber);
            contestProblemUserRepository.increaseContestSubmit(id, problemNumber, userId);
            return recordService.addRecord(userId, problem.getId(), id, problemNumber, problem.getVisibility() == Visibility.PRIVATE, submitLanguage, code);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean checkContestEnded(Integer id) {
        Contest contest = contestRepository.getContestById(id);
        return !(new Date()).before(contest.getEndTime()) && recordRepository.countPendingAndJudgingRecordsByContestId(id) > 0;
    }

    private ArrayList<ContestType> getContestTypes(Boolean showPractice, Boolean showCompetition) {
        if(!showPractice && !showCompetition) {
            return new ArrayList<>(Arrays.asList(ContestType.PRAC, ContestType.COMP));
        }
        ArrayList<ContestType> types = new ArrayList<>();
        if(showPractice) {
            types.add(ContestType.PRAC);
        }
        if(showCompetition) {
            types.add(ContestType.COMP);
        }
        return types;
    }

    private ArrayList<ContestRankHeaderUnit> getContestRankHeaderUnits(Integer contestId) {
        ArrayList<ContestRankHeaderUnit> headerUnits = contestProblemRepository.getContestProblems(contestId);
        for (ContestRankHeaderUnit headerUnit : headerUnits) {
            headerUnit.setTriedParticipant(contestProblemUserRepository.countTriedParticipant(contestId, headerUnit.getProblemNumber()));
            headerUnit.setPassedParticipant(contestProblemUserRepository.countPassedParticipant(contestId, headerUnit.getProblemNumber()));
        }
        return headerUnits;
    }

    private ArrayList<ContestRankEntry> getContestRankEntries(Integer contestId, Integer pageIndex, Integer pageSize, Boolean all) {
        ArrayList<ContestRankEntry> contestRankEntries;
        if(all) {
            contestRankEntries = contestProblemUserRepository.getAllContestRankEntries(contestId);
        } else {
            contestRankEntries = contestProblemUserRepository.getContestRankEntries(contestId, (pageIndex - 1) * pageSize, pageSize);
        }
        for (ContestRankEntry contestRankEntry : contestRankEntries) {
            Participation participation = participationRepository.getParticipation(contestId, contestRankEntry.getUserId());
            contestRankEntry.setUsername(participation.getUsername());
            contestRankEntry.setNickname(participation.getNickname());
            contestRankEntry.setUnits(contestProblemUserRepository.getContestRankUnits(contestId, contestRankEntry.getUserId()));
        }
        return contestRankEntries;
    }

    @Override
    public ContestRank getContestRank(Integer id, Integer pageIndex, Integer pageSize) {
        if(checkContestEnded(id) && Boolean.TRUE.equals(stringRedisTemplate.hasKey("rank:" + id))) {
            ContestRank contestRank = JSON.parseObject(stringRedisTemplate.opsForValue().get("rank:" + id), ContestRank.class);
            contestRank.setEntries(new ArrayList<>(contestRank.getEntries().subList((pageIndex - 1) * pageSize, pageIndex * pageSize)));
            return contestRank;
        }
        ContestRank contestRank = new ContestRank();
        Contest contest = contestRepository.getContestById(id);
        contestRank.setContestId(id);
        contestRank.setProblemAmount(contest.getProblemAmount());
        contestRank.setParticipantAmount(contest.getParticipantAmount());
        contestRank.setHeaderUnits(getContestRankHeaderUnits(id));
        if (checkContestEnded(id) && Boolean.FALSE.equals(stringRedisTemplate.hasKey("rank:" + id.toString()))) {
            contestRank.setEntries(getContestRankEntries(id, null, null, true));
            stringRedisTemplate.opsForValue().set("rank:" + id, JSON.toJSONString(contestRank));
        }
        contestRank.setEntries(getContestRankEntries(id, pageIndex, pageSize, false));
        return contestRank;
    }
}
