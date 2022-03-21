package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Problem;

import java.util.ArrayList;

@Mapper
@Repository
public interface ProblemRepository {

    boolean addProblem(Problem problem);

    Problem getProblemById(Integer id);

    ArrayList<Problem> getProblemsByAuthorId(@Param("authorId") Integer authorId, @Param("startIndex") Integer startIndex, @Param("pageSize") Integer pageSize);

    ArrayList<Problem> getProblemsByName(@Param("name") String name, @Param("startIndex") Integer startIndex, @Param("pageSize") Integer pageSize);
}
