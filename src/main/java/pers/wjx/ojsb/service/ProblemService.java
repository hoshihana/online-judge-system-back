package pers.wjx.ojsb.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import pers.wjx.ojsb.pojo.*;
import pers.wjx.ojsb.pojo.enumeration.Visibility;

import java.util.ArrayList;

public interface ProblemService {
    ArrayList<ProblemEntry> getPublicProblemEntriesByKey(String key, boolean byId, Integer pageIndex, Integer pageSize);

    Integer countProblemEntriesByKey(String key, boolean byId);

    ArrayList<ProblemEntry> getProblemEntriesByAuthorId(Integer authorId, Integer pageIndex, Integer pageSize);

    Integer countProblemEntriesByAuthorId(Integer authorId);

    ProblemUserRelation getProblemUserRelation(Integer userId, Integer problemId);

    Problem getProblemById(Integer id);

    Integer addProblem(Integer authorId, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit, Visibility visibility);

    boolean updateProblem(Integer id, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit, Visibility visibility);

    Integer getAuthorIdById(Integer id);

    boolean deleteProblemById(Integer id);

    TryPassAmountPair getTryPassAmountPairById(Integer id);

    TestFileInfo saveTestFile(Integer id, MultipartFile file);

    boolean deleteTestFile(Integer id);

    TestFileInfo getTestFileInfo(Integer id);

    FileSystemResource getTestFileResource(Integer id);
}
