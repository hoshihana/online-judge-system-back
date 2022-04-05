package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;

import java.util.ArrayList;

@Mapper
@Repository
public interface ProblemRepository {

    boolean addProblem(Problem problem);

    boolean updateProblem(Problem problem);

    Problem getProblemById(Integer id);

    boolean deleteProblemById(Integer id);

    ArrayList<ProblemEntry> getProblemEntriesByAuthorId(@Param("authorId") Integer authorId, @Param("startIndex") Integer startIndex, @Param("pageSize") Integer pageSize);

    ArrayList<ProblemEntry> getProblemEntriesByName(@Param("name") String name, @Param("startIndex") Integer startIndex, @Param("pageSize") Integer pageSize);

    Integer countProblemEntriesByName(String name);

    Integer countProblemEntriesByAuthorId(Integer authorId);

    Integer countProblemEntriesById(Integer id);

    Integer getAuthorIdById(Integer id);

    boolean increaseSubmit(Integer id);

    boolean increaseAccept(Integer id);

    boolean setTestSet(Integer id);

    boolean getTestSet(Integer id);
}
