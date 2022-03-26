package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.ProblemUserRelation;

public interface ProblemUserService {

    ProblemUserRelation getProblemUserRelation(Integer userId, Integer problemId);

}
