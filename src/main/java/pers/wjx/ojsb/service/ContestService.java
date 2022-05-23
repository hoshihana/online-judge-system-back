package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.ContestRankEntry;
import pers.wjx.ojsb.pojo.enumeration.ContestType;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;

import java.util.ArrayList;
import java.util.Date;

public interface ContestService {
    Integer addContest(Integer authorId, String name, ContestType type, String description, Boolean passwordSet, String password, Date startTime, Date endTime);

    boolean updateContestDetail(Integer id, String name, ContestType type, String description, Boolean passwordSet, String password);

    boolean setContestTime(Integer id, Date startTime, Date endTime);

    boolean deleteContest(Integer id);

    boolean resetContest(Integer id, Date startTime, Date endTime);

    boolean setContestEndTime(Integer id, Date endTime);

    boolean participateContest(Integer id, Integer userId, String nickname, String password);

    boolean isContestParticipant(Integer id, Integer userId);

    boolean setContestProblems(Integer id, ArrayList<Integer> problemIds);

    boolean validateProblemIds(Integer authorId, ArrayList<Integer> problemIds);

    ArrayList<ContestProblemUserRelation> getContestProblemUserRelations(Integer id, Integer userId);

    ContestProblemUserRelation getContestProblemUserRelation(Integer id, Integer problemNumber, Integer userId);

    ArrayList<ProblemEntry> getContestProblemEntries(Integer id);

    Problem getContestProblem(Integer id, Integer problemNumber);

    TryPassAmountPair getContestTryPassAmountPair(Integer id, Integer problemNumber);

    Contest getContestById(Integer id);

    ArrayList<Contest> getRecentContests(Integer dayLimit);

    ArrayList<Contest> getContestsByKey(String key, Integer pageIndex, Integer pageSize);

    Integer countContestsByKey(String key);

    ArrayList<Contest> getUserContestsByKey(Integer authorId, String key, Boolean showPractice, Boolean showCompetition, Boolean orderByStartTimeAsc, Integer pageIndex, Integer pageSize);

    Integer countUserContestsByKey(Integer authorId, String key, Boolean showPractice, Boolean showCompetition);

    ArrayList<Record> getContestRecords(Integer id, String problemNumber, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer pageIndex, Integer pageSize);

    Integer countContestRecords(Integer id, String problemNumber, String username, Language submitLanguage, JudgeResult judgeResult);

    ArrayList<Record> getContestProblemRecentRecords(Integer id, Integer problemNumber, Integer userId, Integer limit);

    Record getContestRecord(Integer id, Integer recordId);

    Integer submitCode(Integer id, Integer problemNumber, Problem problem, Integer userId, Language submitLanguage, String code);

    boolean checkContestEnded(Integer id);

    boolean setContestOpen(Integer id);

    ContestRank getContestRank(Integer id, Integer pageIndex, Integer pageSize);

    ContestRankEntry getContestRankEntryByUserId(Integer id, Integer userId);

    boolean updateContestParticipationNickname(Integer id, Integer userId, String nickname);
}
