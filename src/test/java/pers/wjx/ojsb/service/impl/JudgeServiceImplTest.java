package pers.wjx.ojsb.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import pers.wjx.ojsb.pojo.JudgeRequest;
import pers.wjx.ojsb.pojo.JudgeResponse;
import pers.wjx.ojsb.pojo.JudgeResultResponse;
import pers.wjx.ojsb.repository.RecordRepository;
import pers.wjx.ojsb.service.JudgeService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
class JudgeServiceImplTest {

    @Resource
    private JudgeService judgeService;

    @Resource
    private RecordRepository recordRepository;

    @Value("${judge-base-url}")
    private String judgeBaseUrl;

    @Test
    void test() {
        judgeService.checkJudge();
    }

    @Test
    void send() {
        JudgeRequest judgeRequest = new JudgeRequest();
        judgeRequest.setSourceCode("#include <cstdio>\n\nint main(void) {\n  char name[10];\n  scanf(\"%s\", name);\n  printf(\"hello, %s\", name);\n  return 0;\n}");
        judgeRequest.setLanguageId(53);
        judgeRequest.setStdin("world1");
        judgeRequest.setExpectedOutput("hello, world1");
        RestTemplate restTemplate = new RestTemplate();
        JudgeResponse judgeResponse = restTemplate.postForObject(judgeBaseUrl + "/submissions?base64_encoded=true", judgeRequest, JudgeResponse.class);
        System.out.println(judgeResponse);
    }

    @Test
    void check() {
        RestTemplate restTemplate = new RestTemplate();
        JudgeResultResponse judgeResultResponse = restTemplate.getForObject(judgeBaseUrl + "/submissions/7230ab6b-2aaa-42ea-aca8-2922c874f18e?base64_encoded=true", JudgeResultResponse.class);
        System.out.println(judgeResultResponse);
    }
}