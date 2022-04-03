package pers.wjx.ojsb.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.repository.JudgeRepository;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.repository.RecordRepository;
import pers.wjx.ojsb.service.JudgeService;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
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

    private Queue<Judge> judgingJudgeQueue;

    private Record currentRecord;  // 时间单位为ms，内存单位为KB

    private Problem currentProblem; // 时间单位为ms，内存单位为MB

    private Integer currentWallTimeLimit;

    private Integer currentCpuExtraTime;

    private String currentCode;

    private ArrayList<String> currentInputTests;

    private ArrayList<String> currentOutTests;

    private RestTemplate restTemplate;

    private byte[] buffer;

    @Value("${code-location}")
    private String codeLocation;

    @Value("${test-location}")
    private String testLocation;

    @Value("${judge-base-url}")
    private String judgeBaseUrl;

    public JudgeServiceImpl() {
        buffer = new byte[1024];
        restTemplate = new RestTemplate();
        pendingRecordQueue = new LinkedList<>();
        pendingJudgeQueue = new LinkedList<>();
        // 注意此处recordRepository等bean还没注入
        // 初始化时注意为JD状态但存在子任务judge为PD状态的的record（即其子任务只有部分送入了评测机，这做好特殊处理，可删除judge表中已存在的子任务表项？）
    }

    @Override
    // @Scheduled(fixedRate = 500)
    // 每次处理一条记录(record)，将每个测试点分为子任务(judge)送入评测机
    public void sendJudge() {
        if(pendingJudgeQueue.isEmpty()) {
            if(pendingRecordQueue.isEmpty()) {
                return;
            } else {
                currentRecord = pendingRecordQueue.poll();  // todo 修改
                updateCurrentCode(currentRecord);
                updateCurrentTests(currentRecord);
                currentProblem = problemRepository.getProblemById(currentRecord.getProblemId());
                if(currentCode == null || currentProblem == null || currentInputTests.isEmpty() || currentOutTests.isEmpty()) {
                    recordRepository.setJudgeResult(currentRecord.getId(), JudgeResult.SE, null, null);
                    return;
                } else {
                    recordRepository.setJudgeResult(currentRecord.getId(), JudgeResult.JD, null, null);
                }
                currentCpuExtraTime = Math.min((int) (currentProblem.getTimeLimit() * cpuExtraTimeRatio), maxCpuExtraTime);
                currentWallTimeLimit = Math.min((int) (currentProblem.getTimeLimit() * wallTimeLimitRatio), maxWallTimeLimit);
                for(int i = 0; i < currentInputTests.size(); i++) {
                    Judge judge = new Judge();
                    judge.setRecordId(currentRecord.getId());
                    judge.setTestId(i + 1);
                    judge.setJudgeResult(JudgeResult.PD);
                    pendingJudgeQueue.offer(judge);
                    judgeRepository.addJudge(judge.getRecordId(), judge.getTestId());
                }
            }
        }
        JudgeRequestBatch judgeRequestBatch = new JudgeRequestBatch(new ArrayList<JudgeRequest>());
        // 由于限制了题目的测试点数量不超过20，故不会超过max-submission-batch-size(20)
        for(Judge judge : pendingJudgeQueue) {
            JudgeRequest judgeRequest = new JudgeRequest();
            judgeRequest.setSourceCode(currentCode);
            judgeRequest.setLanguageId(getLanguageId(currentRecord.getSubmitLanguage()));
            judgeRequest.setStdin(currentInputTests.get(judge.getTestId() - 1));
            judgeRequest.setExpectedOutput(currentOutTests.get(judge.getTestId() - 1));
            judgeRequest.setCpuTimeLimit(currentProblem.getTimeLimit() / 1000F);
            judgeRequest.setCpuExtraTime(currentCpuExtraTime / 1000F);
            judgeRequest.setWallTimeLimit(currentWallTimeLimit / 1000F);
            judgeRequest.setMemoryLimit(currentProblem.getMemoryLimit() * 1000F);
            judgeRequestBatch.getSubmissions().add(judgeRequest);
        }
        ArrayList<JudgeResponse> judgeResponses = restTemplate.postForObject(judgeBaseUrl + "/submissions/batch?base64_encoded=true", judgeRequestBatch, ArrayList.class);
        for(JudgeResponse judgeResponse : judgeResponses) {
            Judge judge = pendingJudgeQueue.poll();
            if(judgeResponse.token == null) {
                if(judgeResponse.getError().equals("queue is full")) {  // todo 队列已满验证效果待检验
                    pendingJudgeQueue.offer(judge); // 评测机队列已满，子任务重新放入等待队列
                } else {
                    judge.setJudgeResult(JudgeResult.SE);   // 发送错误，子任务无法送入评测机，直接设置为SE
                    judgeRepository.updateJudge(judge);
                }
            } else {
                judge.setJudgeToken(judgeResponse.getToken());
                judge.setJudgeResult(JudgeResult.JD);
                judgeRepository.updateJudge(judge);
                judgingJudgeQueue.offer(judge);
            }
        }
    }

    @Override
    // @Scheduled(fixedRate = 500)
    public void checkJudge() {
        if(!judgingJudgeQueue.isEmpty()) {

        }


        JudgeResultResponseBatch judgeResultResponseBatch = restTemplate.getForObject(judgeBaseUrl + "/submissions/batch?base64_encoded=true&tokens=" +
                "b54c0a06-5e6c-4fea-a4c8-15ace502f7a5,95174852-8ee3-4ca7-aa86-5cd0fe4bdd7f,e51e36c0-bb5a-4112-970b-0b6ea517b912", JudgeResultResponseBatch.class);

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

    private Integer getLanguageId(Language language) {
        switch (language) {
            case C:
                return 50; // GCC 9.2.0
            case CPP:
                return 54;  // GCC 9.2.0
            case JAVA:
                return 62;  // OpenJDK 13.0.1
            case PY:
                return 71;  // 3.8.1
        }
        return null;
    }
}
