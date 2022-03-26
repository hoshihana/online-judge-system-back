package pers.wjx.ojsb.service.impl;

import org.springframework.stereotype.Service;
import pers.wjx.ojsb.pojo.ProblemUserRelation;
import pers.wjx.ojsb.repository.ProblemUserRepository;
import pers.wjx.ojsb.service.ProblemUserService;

import javax.annotation.Resource;

@Service
public class ProblemUserServiceImpl implements ProblemUserService {
    @Resource
    ProblemUserRepository problemUserRepository;

    @Override
    public ProblemUserRelation getProblemUserRelation(Integer userId, Integer problemId) {
        return problemUserRepository.getProblemUserRelation(userId, problemId);
    }

}
