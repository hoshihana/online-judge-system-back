package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ContestProblemUserRepository {
    Integer deleteRelationsByContestId(Integer contestId);
}
