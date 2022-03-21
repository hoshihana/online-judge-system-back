package pers.wjx.ojsb.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pers.wjx.ojsb.pojo.Problem;

import javax.annotation.Resource;

@SpringBootTest
class ProblemRepositoryTest {

    @Resource
    ProblemRepository problemRepository;

    @Test
    void addProblem() {
        Problem problem = new Problem();
        problem.setAuthorId(8);
        problem.setDescription("<s>题目描述   题目描述</s>");
        problem.setInputFormat("<b>输入格式 输入格式</b><br>输入格式");
        problem.setOutputFormat("输出格式<br>输出格式<br>");
        problem.setExplanation("说明说明<br>说明<br>说<br>明");
        problem.setSamples("[{'input': '1 1', 'output': '2'}, {'input': '2 3', 'output': '5'}, {'input': '12\n 5', 'output': '4'}]");
        problem.setTimeLimit(1000);
        problem.setMemoryLimit(125);
        for (int i = 0; i < 499; i++) {
            problem.setId(null);
            problem.setName("题目" + (i + 1));
            problemRepository.addProblem(problem);
        }
    }
}