package pers.wjx.ojsb.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Judge;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.repository.JudgeRepository;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.repository.RecordRepository;
import pers.wjx.ojsb.service.JudgeService;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.zip.ZipInputStream;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private JudgeRepository judgeRepository;

    @Resource
    private ProblemRepository problemRepository;

    @Value("${max-wall-time-limit}")
    private Integer maxWallTimeLimit;

    @Value("${max-cpu-extra-time}")
    private Integer maxCpuExtraTime;

    @Value("${wall-time-limit-ratio}")
    private Float wallTimeLimitRatio;

    @Value("${cpu-extra-time-ratio}")
    private Float cpuExtraTimeRatio;

    @Value("${max-submission-batch-size}")
    private Integer maxSubmissionBatchSize;

    private Queue<Record> pendingRecordQueue;

    private Queue<Judge> pendingJudgeQueue;

    private Record currentRecord;

    private Problem currentProblem;

    private Integer currentWallTimeLimit;

    private Integer currentCpuExtraTime;

    private String currentCode;

    private ArrayList<String> currentInputTests;

    private ArrayList<String> currentOutTests;

    private byte[] buffer;

    @Value("${code-location}")
    private String codeLocation;

    @Value("${test-location}")
    private String testLocation;

    public JudgeServiceImpl() {
        buffer = new byte[1024];
        // 初始化队列
    }

    @Override
   // @Scheduled(fixedRate = 1000)
    public void sendJudge() {
        if(pendingJudgeQueue.isEmpty()) {
            if(pendingRecordQueue.isEmpty()) {
                return;
            } else {
                currentRecord = pendingRecordQueue.poll();
                updateCurrentCode(currentRecord);
                updateCurrentTests(currentRecord);
                currentProblem = problemRepository.getProblemById(currentRecord.getProblemId());
                if(currentCode == null || currentProblem == null || currentInputTests.isEmpty() || currentOutTests.isEmpty()) {
                    recordRepository.setJudgeResult(currentRecord.getId(), JudgeResult.SE, null, null);
                    return;
                }
                currentCpuExtraTime = (int) (currentProblem.getTimeLimit() * cpuExtraTimeRatio);
                currentWallTimeLimit = (int) (currentProblem.getTimeLimit() * wallTimeLimitRatio);
                for(int i = 0; i < currentInputTests.size(); i++) {
                    Judge judge = new Judge();
                    judge.setRecordId(currentRecord.getId());
                    judge.setTestId(i + 1);
                    pendingJudgeQueue.add(judge);
                    judgeRepository.addJudge(judge.getRecordId(), judge.getTestId());
                }
            }
        }
        while(!pendingJudgeQueue.isEmpty()) {
            Judge currentJudge = pendingJudgeQueue.peek();

        }
    }

    private void updateCurrentCode(Record record) {
        currentCode = null;
        String fileName = record.getId() + "." + record.getSubmitLanguage().name().toLowerCase();
        char[] code = new char[record.getCodeLength()];
        try {
            FileReader reader = new FileReader(codeLocation + fileName);
            reader.read(code);
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        currentCode = new String(code);
    }

    private void updateCurrentTests(Record record) {
        currentInputTests = new ArrayList<>();
        currentOutTests = new ArrayList<>();
        String fileName = record.getProblemId() + ".zip";
        ArrayList<String> tests = new ArrayList<>();
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(testLocation + fileName)));
            boolean flag = true;
            while (zipInputStream.getNextEntry() != null) {
                int size = 0;
                StringBuilder test = new StringBuilder();
                while((size = zipInputStream.read(buffer)) > 0) {
                    test.append(new String(buffer, 0, size));
                }
                if(flag) {
                    currentInputTests.add(test.toString());
                } else {
                    currentOutTests.add(test.toString());
                }
                flag = !flag;
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
