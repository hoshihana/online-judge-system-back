package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.ContestProblemUserRelation;

import java.util.ArrayList;

@Repository
@Mapper
public interface ContestProblemUserRepository {
    Integer deleteRelationsByContestId(Integer contestId);

    ArrayList<ContestProblemUserRelation> getRelationsByContestIdAndUserId(Integer contestId, Integer userId);
}
