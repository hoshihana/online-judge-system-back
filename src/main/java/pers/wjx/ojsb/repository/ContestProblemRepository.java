package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ContestProblemRepository {
    boolean deleteContestProblemsByContestId(Integer contestId);

    boolean addContestProblem(Integer contestId, Integer problemId,Integer problemNumber);
}
