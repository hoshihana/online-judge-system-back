package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemBrief;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Resource
    private ProblemRepository problemRepository;

    @Override
    public ArrayList<ProblemBrief> getProblemBriefsByKey(String key, boolean byId, Integer pageIndex, Integer pageSize) {
        key = key.trim();
        ArrayList<ProblemBrief> problemBriefs = new ArrayList<>();
        if (byId) {
            if (Pattern.matches("^\\d{1,8}$", key)) {
                Problem problem = problemRepository.getProblemById(Integer.valueOf(key));
                if (problem != null) {
                    problemBriefs.add(new ProblemBrief(problem.getId(), problem.getName(), problem.getSubmit(), problem.getAccept()));
                }
            }
        } else {
            problemBriefs = problemRepository.getProblemBriefsByName(key, (pageIndex - 1) * pageSize, pageSize);
        }
        return problemBriefs;
    }

    @Override
    public Integer countProblemBriefsByKey(String key, boolean byId) {
        key = key.trim();
        if(byId) {
            if (Pattern.matches("^\\d{1,8}$", key) && problemRepository.getProblemById(Integer.valueOf(key)) != null) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return problemRepository.countProblemsByName(key);
        }
    }

    @Override
    public Problem getProblemById(Integer id) {
        return problemRepository.getProblemById(id);
    }

    @Override
    public Integer addProblem(Integer authorId, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit) {
        Problem problem = new Problem();
        problem.setAuthorId(authorId);
        problem.setName(name);
        problem.setDescription(description);
        problem.setInputFormat(inputFormat);
        problem.setOutputFormat(outputFormat);
        problem.setExplanation(explanation);
        problem.setSamples(samples);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);
        if(problemRepository.addProblem(problem)) {
            return problem.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean updateProblem(Integer id, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setName(name);
        problem.setDescription(description);
        problem.setInputFormat(inputFormat);
        problem.setOutputFormat(outputFormat);
        problem.setExplanation(explanation);
        problem.setSamples(samples);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);
        return problemRepository.updateProblem(problem);
    }

    @Override
    public Integer getAuthorIdById(Integer id) {
        return problemRepository.getAuthorIdById(id);
    }
}
