package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.enumeration.ContestType;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;

import java.util.ArrayList;
import java.util.Date;

public interface ContestService {
    Integer addContest(Integer authorId, String name, ContestType type, String description, Boolean passwordSet, String password, Date startTime, Date endTime);

    boolean updateContestDetail(Integer id, String name, ContestType type, String description, Boolean passwordSet, String password);

    boolean setContestTime(Integer id, Date startTime, Date endTime);

    boolean resetContest(Integer id, Date startTime, Date endTime);

    boolean setContestEndTime(Integer id, Date endTime);

    boolean participateContest(Integer id, Integer userId, String nickname, String password);

    boolean isContestParticipant(Integer id, Integer userId);

    boolean setContestProblems(Integer id, ArrayList<Integer> problemIds);

    boolean validateProblemIds(Integer authorId, ArrayList<Integer> problemIds);

    ArrayList<ContestProblemUserRelation> getContestProblemUserRelations(Integer id, Integer userId);

    ArrayList<ProblemEntry> getContestProblemEntries(Integer id);

    Problem getContestProblem(Integer id, Integer problemNumber);

    Contest getContestById(Integer id);

    ArrayList<Contest> getUserContestsByKey(Integer authorId, String key, Boolean showPractice, Boolean showCompetition, Boolean orderByStartTimeAsc, Integer pageIndex, Integer pageSize);

    Integer countUserContestsByKey(Integer authorId, String key, Boolean showPractice, Boolean showCompetition);

    ArrayList<Record> getContestRecords(Integer id, String problemNumber, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer pageIndex, Integer pageSize);

    Integer countContestRecords(Integer id, String problemNumber, String username, Language submitLanguage, JudgeResult judgeResult);

    Record getContestRecord(Integer id, Integer recordId);
}
