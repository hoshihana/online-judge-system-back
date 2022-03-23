package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemBrief;

import java.util.ArrayList;

@Mapper
@Repository
public interface ProblemRepository {

    boolean addProblem(Problem problem);

    boolean updateProblem(Problem problem);

    Problem getProblemById(Integer id);

    ArrayList<ProblemBrief> getProblemBriefsByAuthorId(@Param("authorId") Integer authorId, @Param("startIndex") Integer startIndex, @Param("pageSize") Integer pageSize);

    ArrayList<ProblemBrief> getProblemBriefsByName(@Param("name") String name, @Param("startIndex") Integer startIndex, @Param("pageSize") Integer pageSize);

    Integer countProblemsByName(String name);

    Integer getAuthorIdById(Integer id);
}
