package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.ProblemUserRelation;

@Mapper
@Repository
public interface ProblemUserRepository {

    boolean increaseSubmit(Integer userId, Integer problemId);

    boolean increaseAccept(Integer userId, Integer problemId);

    ProblemUserRelation getProblemUserRelation(Integer userId, Integer problemId);

    Integer countTriedUserByProblemId(Integer problemId);

    Integer countPassedUserByProblemId(Integer problemId);

    Integer countTriedProblemByUserId(Integer userId);

    Integer countPassedProblemByUserId(Integer userId);
}
