package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;
import pers.wjx.ojsb.pojo.enumeration.Visibility;

import java.util.ArrayList;

@Mapper
@Repository
public interface ProblemRepository {

    boolean addProblem(Problem problem);

    boolean updateProblem(Problem problem);

    Problem getProblemById(Integer id);

    boolean deleteProblemById(Integer id);

    ArrayList<ProblemEntry> getPublicProblemEntriesByName(String name, Integer startIndex, Integer pageSize);

    Integer countPublicProblemEntriesByName(String name);

    ArrayList<ProblemEntry> getUserProblemEntriesByName(Integer authorId, String name, ArrayList<Visibility> visibilities, Integer startIndex, Integer pageSize);

    ArrayList<ProblemEntry> getAllUserProblemEntriesByName(Integer authorId, String name, ArrayList<Visibility> visibilities);

    Integer countUserProblemEntriesByName(Integer authorId, String name, ArrayList<Visibility> visibilities);

    Integer getAuthorIdById(Integer id);

    boolean increaseSubmit(Integer id);

    boolean increaseAccept(Integer id);

    boolean setTestSet(Integer id);

    boolean getTestSet(Integer id);

    Integer getMemoryLimit(Integer id);
}
