package pers.wjx.ojsb.service.impl;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import pers.wjx.ojsb.pojo.Contest;
import pers.wjx.ojsb.repository.ContestRepository;

import javax.annotation.Resource;


@SpringBootTest
class ProblemServiceImplTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ContestRepository contestRepository;

    @Test
    void getProblemBriefByKey() {
        System.out.println(JSON.parse(stringRedisTemplate.opsForValue().get("contest100")));
        // stringRedisTemplate.opsForValue().set("contest100", JSON.toJSONString(contestRepository.getContestById(100)));
    }
}