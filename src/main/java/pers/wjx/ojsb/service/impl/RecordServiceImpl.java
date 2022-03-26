package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.repository.AccountRepository;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.repository.RecordRepository;
import pers.wjx.ojsb.repository.ProblemUserRepository;
import pers.wjx.ojsb.service.RecordService;

import javax.annotation.Resource;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

@Service
public class RecordServiceImpl implements RecordService {

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private AccountRepository accountRepository;

    @Resource
    private ProblemRepository problemRepository;

    @Resource
    private ProblemUserRepository problemUserRepository;

    public static final String codeLocation = "C:\\Users\\81050\\Desktop\\submit-code\\";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addRecord(Integer userId, Integer problemId, Language submitLanguage, String code) {
        Record record = new Record();
        record.setUserId(userId);
        record.setUsername(accountRepository.getUsernameById(userId));
        record.setProblemId(problemId);
        record.setSubmitTime(new Date());
        record.setCodeLength(code.length());
        record.setSubmitLanguage(submitLanguage);
        try {
            recordRepository.addRecord(record);
            if (record.getId() == null) {
                return null;
            }
            problemRepository.increaseSubmit(problemId);
            problemUserRepository.increaseSubmit(userId, problemId);
            String fileName = record.getId() + "." + submitLanguage.name().toLowerCase();
            FileWriter writer = new FileWriter(codeLocation + fileName);
            writer.write(code);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            ex.printStackTrace();
            throw new InternalServerErrorException("代码上传失败");
        }
        return record.getId();
    }

    @Override
    public ArrayList<Record> getRecords(String problemId, String username, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc, Integer pageIndex, Integer pageSize) {
        Integer problemIdInt = null;
        if (Pattern.matches("^\\d{1,8}$", problemId)) {
            problemIdInt = Integer.valueOf(problemId);
        }
        return recordRepository.getRecords(problemIdInt, username, submitLanguage, judgeResult, orderBy, asc, (pageIndex - 1) * pageSize, pageSize);
    }

    @Override
    public Integer countRecords(String problemId, String username, Language submitLanguage, JudgeResult judgeResult) {
        Integer problemIdInt = null;
        if (Pattern.matches("^\\d{1,8}$", problemId)) {
            problemIdInt = Integer.valueOf(problemId);
        }
        return recordRepository.countRecords(problemIdInt, username, submitLanguage, judgeResult);
    }

    @Override
    public Record getRecordById(Integer id) {
        return recordRepository.getRecordById(id);
    }

    @Override
    public String getCode(Integer id, Language submitLanguage, Integer codeLength) {
        String location = "C:\\Users\\81050\\Desktop\\submit-code\\";
        String fileName = id + "." + submitLanguage.name().toLowerCase();
        char[] code = new char[codeLength];
        try {
            FileReader reader = new FileReader(location + fileName);
            reader.read(code);
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalServerErrorException("代码读取失败");
        }
        return new String(code);
    }

    @Override
    public ArrayList<Record> getRecentRecords(Integer problemId, Integer userId, Integer limit) {
        return recordRepository.getRecentRecords(problemId, userId, limit);
    }
}
