package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.ContestProblemUserRelation;

import java.util.ArrayList;

@Repository
@Mapper
public interface ContestProblemUserRepository {
    Integer deleteRelationsByContestId(Integer contestId);

    Boolean addRelation(Integer contestId, Integer problemId, Integer userId, Integer problemNumber);

    ArrayList<ContestProblemUserRelation> getRelationsByContestIdAndUserId(Integer contestId, Integer userId);
}
