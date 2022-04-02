package pers.wjx.ojsb.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.repository.RecordRepository;
import pers.wjx.ojsb.service.JudgeService;

import javax.annotation.Resource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JudgeServiceImplTest {

    @Resource
    private JudgeServiceImpl judgeService;

    @Resource
    private RecordRepository recordRepository;

    @Test
    void test() {

    }
}