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
            for (Problem problem : problemRepository.getProblemsByName(key, (pageIndex - 1) * pageSize, pageSize)) {
                problemBriefs.add(new ProblemBrief(problem.getId(), problem.getName(), problem.getSubmit(), problem.getAccept()));
            }
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
}
