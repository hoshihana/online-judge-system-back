package pers.wjx.ojsb.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProblemServiceImplTest {

    @Resource
    private ProblemService problemService;

    @Test
    void getProblemBriefByKey() {
        System.out.println(problemService);
    }
}