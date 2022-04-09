package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;

import java.util.ArrayList;

@Repository
@Mapper
public interface ContestProblemRepository {
    boolean deleteContestProblemsByContestId(Integer contestId);

    ArrayList<ProblemEntry> getContestProblemEntries(Integer contestId);

    Problem getContestProblem(Integer contestId, Integer problemNumber);

    boolean addContestProblem(Integer contestId, Integer problemId, Integer problemNumber);
}
