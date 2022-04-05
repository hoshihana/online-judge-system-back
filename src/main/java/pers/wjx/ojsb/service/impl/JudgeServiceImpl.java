package pers.wjx.ojsb.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.repository.JudgeRepository;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.repository.ProblemUserRepository;
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

    @Resource
    private ProblemUserRepository problemUserRepository;

    @Value("${max-wall-time-limit}")
    private Integer maxWallTimeLimit;

    @Value("${min-wall-time-limit}")
    private Integer minWallTimeLimit;

    @Value("${max-cpu-extra-time}")
    private Integer maxCpuExtraTime;

    @Value("${wall-time-limit-ratio}")
    private Float wallTimeLimitRatio;

    @Value("${cpu-extra-time-ratio}")
    private Float cpuExtraTimeRatio;

    @Value("${max-submission-batch-size}")
    private Integer maxSubmissionBatchSize;

    @Value("${send-waiting-interval}")
    private Long sendWaitingInterval;

    @Value("${check-waiting-interval}")
    private Long checkWaitingInterval;

    @Value("${judge-result-response-fields}")
    private String judgeResultResponseFields;

    private final Queue<Record> pendingRecordQueue = new LinkedList<>();

    private final Queue<Judge> pendingJudgeQueue = new LinkedList<>();

    private final Queue<Judge> judgingJudgeQueue = new LinkedList<>();

    private final Queue<Judge> checkingJudgeQueue = new LinkedList<>();

    private final RestTemplate restTemplate = new RestTemplate();

    private final byte[] buffer = new byte[1024];

    private Record currentRecord;  // 时间单位为ms，内存单位为KB

    private Problem currentProblem; // 时间单位为ms，内存单位为MB

    private Integer currentCpuTimeLimit;

    private Integer currentWallTimeLimit;

    private Integer currentCpuExtraTime;

    private String currentCode;

    private ArrayList<String> currentInputTests;

    private ArrayList<String> currentOutTests;

    @Value("${code-location}")
    private String codeLocation;

    @Value("${test-location}")
    private String testLocation;

    @Value("${judge-base-url}")
    private String judgeBaseUrl;

    @Override
    @Async
    // 每次处理一条记录(record)，将每个测试点分为子任务(judge)送入评测机
    public void sendJudge() {
        // 初始化，取出数据库中所有Pending和Judging的record，并将Judging的record置为Pending，同时删除其在judge表的中的子任务表项
        ArrayList<Record> pendingRecords = recordRepository.getRecordsByJudgeResult(JudgeResult.PD);
        ArrayList<Record> judgingRecords = recordRepository.getRecordsByJudgeResult(JudgeResult.JD);
        for (Record record : judgingRecords) {
            judgeRepository.deleteJudgesByRecordId(record.getId());
            recordRepository.setJudgeResult(record.getId(), JudgeResult.PD, null, null);
        }
        synchronized (pendingRecordQueue) {
            for (Record record : judgingRecords) {
                pendingRecordQueue.offer(record);
            }
            for (Record record : pendingRecords) {
                pendingRecordQueue.offer(record);
            }
            pendingRecordQueue.notify();
        }
        while (true) {
            // System.out.println("sendJudge...");
            synchronized (pendingRecordQueue) {
                if (pendingRecordQueue.isEmpty()) {
                    try {
                        //     System.out.println("sendJudgeWaitStart...");
                        pendingRecordQueue.wait();
                        //      System.out.println("sendJudgeWaitEnd...");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                currentRecord = pendingRecordQueue.poll();
            }
            updateCurrentCode(currentRecord);
            updateCurrentTests(currentRecord);
            currentProblem = problemRepository.getProblemById(currentRecord.getProblemId());
            if (currentCode == null || currentProblem == null || currentInputTests.isEmpty() || currentOutTests.isEmpty()) {
                recordRepository.setJudgeResult(currentRecord.getId(), JudgeResult.SE, null, null);
                continue;
            } else {
                recordRepository.setJudgeResult(currentRecord.getId(), JudgeResult.JD, null, null);
                recordRepository.setTestAmount(currentRecord.getId(), currentInputTests.size());
            }
            // Java有两倍的时间限制
            currentCpuTimeLimit = currentRecord.getSubmitLanguage() == Language.JAVA ? 2 * currentProblem.getTimeLimit() : currentProblem.getTimeLimit();
            currentCpuExtraTime = Math.min((int) (currentCpuTimeLimit * cpuExtraTimeRatio), maxCpuExtraTime);
            currentWallTimeLimit = Math.max(Math.min((int) (currentCpuTimeLimit * wallTimeLimitRatio), maxWallTimeLimit), minWallTimeLimit);
            for (int i = 0; i < currentInputTests.size(); i++) {
                Judge judge = new Judge();
                judge.setRecordId(currentRecord.getId());
                judge.setTestId(i + 1);
                judge.setJudgeResult(JudgeResult.PD);
                pendingJudgeQueue.offer(judge);
                judgeRepository.addJudge(judge.getRecordId(), judge.getTestId());
            }
            while (true) {
                ArrayList<JudgeRequest> judgeRequests = new ArrayList<>();
                // 由于限制了题目的测试点数量不超过20，故不会超过max-submission-batch-size(20)
                for (Judge judge : pendingJudgeQueue) {
                    JudgeRequest judgeRequest = new JudgeRequest();
                    judgeRequest.setSourceCode(currentCode);
                    judgeRequest.setLanguageId(getLanguageId(currentRecord.getSubmitLanguage()));
                    judgeRequest.setCompilerOptions(getCompileOptions(currentRecord.getSubmitLanguage()));
                    judgeRequest.setStdin(currentInputTests.get(judge.getTestId() - 1));
                    judgeRequest.setExpectedOutput(currentOutTests.get(judge.getTestId() - 1));
                    judgeRequest.setCpuTimeLimit(currentCpuTimeLimit / 1000F);
                    judgeRequest.setCpuExtraTime(currentCpuExtraTime / 1000F);
                    judgeRequest.setWallTimeLimit(currentWallTimeLimit / 1000F);
                    judgeRequest.setMemoryLimit(currentProblem.getMemoryLimit() * 1000F);
                    judgeRequests.add(judgeRequest);
                }
                try {
                    List<JudgeResponse> judgeResponses = Arrays.asList(
                            restTemplate.postForObject(judgeBaseUrl + "/submissions/batch?base64_encoded=true"
                                    , new JudgeRequestBatch(judgeRequests.toArray(new JudgeRequest[judgeRequests.size()]))
                                    , JudgeResponse[].class));
                    for (JudgeResponse judgeResponse : judgeResponses) {
                        Judge judge = pendingJudgeQueue.poll();
                        if (judgeResponse.token == null) {
                            if (judgeResponse.getError().equals("queue is full")) {  // todo 队列已满验证效果待检验
                                pendingJudgeQueue.offer(judge); // 评测机队列已满，子任务重新放入等待队列
                            } else {
                                judge.setJudgeResult(JudgeResult.SE);   // 发送错误，子任务无法送入评测机，直接设置为SE
                                judgeRepository.updateJudge(judge);
                                recordRepository.setJudgeResult(judge.getRecordId(), judge.getJudgeResult(), judge.getExecuteTime(), judge.getExecuteMemory());
                            }
                        } else {
                            judge.setJudgeToken(judgeResponse.getToken());
                            judge.setJudgeResult(JudgeResult.JD);
                            judgeRepository.updateJudge(judge);
                            synchronized (judgingJudgeQueue) {
                                judgingJudgeQueue.offer(judge);
                                judgingJudgeQueue.notify();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    recordRepository.setJudgeResult(currentRecord.getId(), JudgeResult.SE, null, null);
                    judgeRepository.deleteJudgesByRecordId(currentRecord.getId());
                    pendingJudgeQueue.clear();
                }
                if (pendingJudgeQueue.isEmpty()) {
                    break;
                } else {
                    try {
                        Thread.sleep(sendWaitingInterval);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    @Async
    public void offerPendingRecord(Record pendingRecord) {
        synchronized (pendingRecordQueue) {
            pendingRecordQueue.offer(pendingRecord);
            pendingRecordQueue.notify();
        }
    }

    @Override
    @Async
    public void checkJudge() {
        while (true) {
            // System.out.println("checkJudge...");
            synchronized (judgingJudgeQueue) {
                if (judgingJudgeQueue.isEmpty()) {
                    try {
                        //      System.out.println("checkJudgeWaitStart...");
                        judgingJudgeQueue.wait();
                        //     System.out.println("checkJudgeWaitEnd...");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                while (!judgingJudgeQueue.isEmpty() && checkingJudgeQueue.size() < maxSubmissionBatchSize) {
                    checkingJudgeQueue.offer(judgingJudgeQueue.poll());
                }
            }
            while (true) {
                ArrayList<String> tokens = new ArrayList<>();
                for (Judge judge : checkingJudgeQueue) {
                    tokens.add(judge.getJudgeToken());
                }
                JudgeResultResponseBatch judgeResultResponseBatch = restTemplate.getForObject(judgeBaseUrl + "/submissions/batch?base64_encoded=true&tokens=" +
                        String.join(",", tokens) + "&fields=" + judgeResultResponseFields, JudgeResultResponseBatch.class);
                List<JudgeResultResponse> judgeResultResponses = Arrays.asList(judgeResultResponseBatch.getSubmissions());
                for (JudgeResultResponse judgeResultResponse : judgeResultResponses) {
                    Judge judge = checkingJudgeQueue.poll();
                    if (getJudgeResult(judgeResultResponse.getStatus().getId()) == JudgeResult.JD) {
                        checkingJudgeQueue.offer(judge);
                    } else {
                        judge.setJudgeResult(getJudgeResult(judgeResultResponse.getStatus().getId()));
                        if (judgeResultResponse.getTime() != null) {
                            judge.setExecuteTime((int) (judgeResultResponse.getTime() * 1000F));
                        }
                        if (judgeResultResponse.getMemory() != null) {
                            judge.setExecuteMemory(judgeResultResponse.getMemory().intValue());
                        }
                        judgeRepository.updateJudge(judge);
                        if (judge.getJudgeResult() == JudgeResult.AC) {
                            recordRepository.increaseAcceptedTestAmount(judge.getRecordId());
                            Record record = recordRepository.getRecordById(judge.getRecordId());
                            if (record.getTestAmount().equals(record.getAcceptedTestAmount())) {
                                ArrayList<Judge> acceptedJudges = judgeRepository.getJudgesByRecordId(record.getId());
                                int maxTime = 0, maxMemory = 0;
                                for (Judge acceptedJudge : acceptedJudges) {
                                    maxTime = Math.max(maxTime, acceptedJudge.getExecuteTime());
                                    maxMemory = Math.max(maxMemory, acceptedJudge.getExecuteMemory());
                                }
                                recordRepository.setJudgeResult(record.getId(), JudgeResult.AC, maxTime, maxMemory);
                                problemRepository.increaseAccept(record.getProblemId());
                                problemUserRepository.increaseAccept(record.getUserId(), record.getProblemId());
                            }
                        } else {
                            recordRepository.setJudgeResult(judge.getRecordId(), judge.getJudgeResult(), judge.getExecuteTime(), judge.getExecuteMemory());
                        }
                    }
                }
                if (checkingJudgeQueue.isEmpty()) {
                    break;
                } else {
                    try {
                        Thread.sleep(checkWaitingInterval);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void updateCurrentCode(Record record) {
        currentCode = null;
        String fileName = record.getId() + getCodeFileSuffix(record.getSubmitLanguage());
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
                while ((size = zipInputStream.read(buffer)) > 0) {
                    test.append(new String(buffer, 0, size));
                }
                if (flag) {
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
                return 50; // C (GCC 9.2.0)
            case CPP:
            case CPP11:
            case CPP14:
            case CPP17:
                return 54;  // C++ (GCC 9.2.0)
            case JAVA:
                return 62;  // Java (OpenJDK 13.0.1)
            case PY2:
                return 70;  // Python (2.7.17)
            case PY3:
                return 71;  // Python (3.8.1)
        }
        return null;
    }

    private String getCompileOptions(Language language) {
        switch (language) {
            case C:
                return "-O2 -w -fmax-errors=3 -std=c11 -lm";    // gcc
            case CPP:
                return "-O2 -w -fmax-errors=3 -lm";
            case CPP11:
                return "-O2 -w -fmax-errors=3 -std=c++11 -lm";  // g++
            case CPP14:
                return "-O2 -w -fmax-errors=3 -std=c++14 -lm";  // g++
            case CPP17:
                return "-O2 -w -fmax-errors=3 -std=c++17 -lm";  // g++
            case JAVA:
                return "-encoding UTF8";  // javac
            case PY2:
            case PY3:
                return null;
        }
        return null;
    }

    private String getCodeFileSuffix(Language language) {
        switch (language) {
            case C:
                return ".c";
            case CPP:
            case CPP11:
            case CPP14:
            case CPP17:
                return ".cpp";
            case JAVA:
                return ".java";
            case PY2:
            case PY3:
                return ".py";
        }
        return "";
    }

    private JudgeResult getJudgeResult(Integer statusId) {
        switch (statusId) {
            case 1:
            case 2:
                return JudgeResult.JD;
            case 3:
                return JudgeResult.AC;
            case 4:
                return JudgeResult.WA;
            case 5:
                return JudgeResult.TLE;
            case 6:
                return JudgeResult.CE;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return JudgeResult.RE;  // todo 设定MLE
            case 13:
            case 14:
            default:
                return JudgeResult.SE;
        }
    }
}
