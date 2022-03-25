package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;
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
    public ArrayList<ProblemEntry> getProblemEntriesByKey(String key, boolean byId, Integer pageIndex, Integer pageSize) {
        key = key.trim();
        ArrayList<ProblemEntry> problemEntries = new ArrayList<>();
        if (byId) {
            if (Pattern.matches("^\\d{1,8}$", key)) {
                Problem problem = problemRepository.getProblemById(Integer.valueOf(key));
                if (problem != null) {
                    problemEntries.add(new ProblemEntry(problem.getId(), problem.getName(), problem.getSubmit(), problem.getAccept()));
                }
            }
        } else {
            problemEntries = problemRepository.getProblemEntriesByName(key, (pageIndex - 1) * pageSize, pageSize);
        }
        return problemEntries;
    }

    @Override
    public Integer countProblemEntriesByKey(String key, boolean byId) {
        key = key.trim();
        if(byId) {
            if (Pattern.matches("^\\d{1,8}$", key) && problemRepository.getProblemById(Integer.valueOf(key)) != null) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return problemRepository.countProblemEntriesByName(key);
        }
    }

    @Override
    public ArrayList<ProblemEntry> getProblemEntriesByAuthorId(Integer authorId, Integer pageIndex, Integer pageSize) {
        return problemRepository.getProblemEntriesByAuthorId(authorId, (pageIndex - 1) * pageSize, pageSize);
    }

    @Override
    public Integer countProblemEntriesByAuthorId(Integer authorId) {
        return problemRepository.countProblemEntriesByAuthorId(authorId);
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

    @Override
    public boolean deleteProblemById(Integer id) {
        return problemRepository.deleteProblemById(id);
    }

    @Override
    public boolean existProblem(Integer id) {
        return problemRepository.countProblemEntriesById(id) > 0;
    }
}
