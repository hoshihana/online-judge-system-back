package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.TryPassAmountPair;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;

import java.util.ArrayList;

@Mapper
@Repository
public interface RecordRepository {

    boolean addRecord(Record record);

    ArrayList<Record> getRecords(Integer problemId, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer startIndex, Integer pageSize);

    Integer countRecords(Integer problemId, String username, Language submitLanguage, JudgeResult judgeResult);

    ArrayList<Record> getContestRecords(Integer contestId, Integer problemNumber, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer startIndex, Integer pageSize);

    Integer countContestRecords(Integer contestId, Integer problemNumber, String username, Language submitLanguage, JudgeResult judgeResult);

    ArrayList<Record> getContestRecentRecords(Integer contestId, Integer problemNumber, Integer userId, Integer limit);

    Record getRecord(Integer id);

    Record getContestRecord(Integer contestId, Integer recordId);

    ArrayList<Record> getRecentRecords(Integer problemId, Integer userId, Integer limit);

    ArrayList<Record> getRecordsByJudgeResult(JudgeResult judgeResult);

    boolean setTestAmount(Integer id, Integer testAmount);

    boolean increaseAcceptedTestAmount(Integer id);

    boolean setRecordsPersonalByProblemId(Integer problemId, Boolean personal);

    boolean setContestRecordsPersonalByProblemId(Integer problemId, Boolean personal);

    boolean setJudgeResult(Integer id, JudgeResult judgeResult, Integer executeTime, Integer executeMemory);

    boolean setCompileOutput(Integer id, String compileOutput);

    Integer deleteRecordsByContestId(Integer contestId);

    Integer countRecordsByDay(String dayString);

    Integer countAcceptedRecordsByDay(String dayString);

    Integer countPendingAndJudgingRecordsByContestId(Integer contestId);
}
