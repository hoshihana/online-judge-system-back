package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;

import java.util.ArrayList;

public interface ProblemService {
    ArrayList<ProblemEntry> getProblemEntriesByKey(String key, boolean byId, Integer pageIndex, Integer pageSize);

    Integer countProblemEntriesByKey(String key, boolean byId);

    ArrayList<ProblemEntry> getProblemEntriesByAuthorId(Integer authorId, Integer pageIndex, Integer pageSize);

    Integer countProblemEntriesByAuthorId(Integer authorId);

    Problem getProblemById(Integer id);

    Integer addProblem(Integer authorId, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit);

    boolean updateProblem(Integer id, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit);

    Integer getAuthorIdById(Integer id);

    boolean deleteProblemById(Integer id);
}
