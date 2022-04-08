package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.pojo.enumeration.Visibility;

import java.util.ArrayList;

@Mapper
@Repository
public interface RecordRepository {

    boolean addRecord(Record record);

    ArrayList<Record> getRecords(Visibility visibility, Integer problemId, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer startIndex, Integer pageSize);

    Integer countRecords(Visibility visibility, Integer problemId, String username, Language submitLanguage, JudgeResult judgeResult);

    Record getRecordById(Integer id);

    ArrayList<Record> getRecentRecords(Integer problemId, Integer userId, Integer limit);

    ArrayList<Record> getRecordsByJudgeResult(JudgeResult judgeResult);

    boolean setTestAmount(Integer id, Integer testAmount);

    boolean increaseAcceptedTestAmount(Integer id);

    boolean setVisibilityByProblemId(Integer problemId, Visibility visibility);

    boolean setJudgeResult(Integer id, JudgeResult judgeResult, Integer executeTime, Integer executeMemory);

    boolean setCompileOutput(Integer id, String compileOutput);
}
