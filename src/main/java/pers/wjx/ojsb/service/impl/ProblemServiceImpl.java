package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemBrief;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import java.util.ArrayList;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Resource
    private ProblemRepository problemRepository;

    @Override
    public ArrayList<ProblemBrief> getProblemBriefByKey(String key, Integer pageIndex, Integer pageSize) {
        Problem problem = null;
        ArrayList<ProblemBrief> problemBriefs = new ArrayList<>();
        try {
            problem = problemRepository.getProblemById(Integer.valueOf(key));
        } catch (Exception e) {
            problem = null;
        } finally {
            if (problem != null) {
                problemBriefs.add(new ProblemBrief(problem.getId(), problem.getName(), problem.getSubmit(), problem.getAccept()));
            } else {
                for (Problem problem_ : problemRepository.getProblemsByName(key, (pageIndex - 1) * pageSize, pageSize)) {
                    problemBriefs.add(new ProblemBrief(problem_.getId(), problem_.getName(), problem_.getSubmit(), problem_.getAccept()));
                }
            }
            return problemBriefs;
        }
    }
}
