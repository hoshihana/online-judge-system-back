package pers.wjx.ojsb.service;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.pojo.enumeration.Visibility;

import java.util.ArrayList;

@Service
public interface RecordService {

    void startJudgeService();

    Integer addRecord(Integer userId, Integer problemId, Integer contestId, Visibility visibility, Language submitLanguage, String code);

    ArrayList<Record> getPublicRecords(String problemId, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer pageIndex, Integer pageSize);

    Integer countPublicRecords(String problemId, String username, Language submitLanguage, JudgeResult judgeResult);

    Record getRecordById(Integer id);

    String getCode(Integer id, Language submitLanguage, Integer codeLength);

    ArrayList<Record> getRecentRecords(Integer problemId, Integer userId, Integer limit);
}
