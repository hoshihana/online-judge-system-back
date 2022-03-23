package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemBrief;

import java.util.ArrayList;

public interface ProblemService {
    ArrayList<ProblemBrief> getProblemBriefsByKey(String key, boolean byId, Integer pageIndex, Integer pageSize);

    Integer countProblemBriefsByKey(String key, boolean byId);

    Problem getProblemById(Integer id);

    Integer addProblem(Integer authorId, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit);

    boolean updateProblem(Integer id, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit);

    Integer getAuthorIdById(Integer id);
}
