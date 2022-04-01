package pers.wjx.ojsb.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;
import pers.wjx.ojsb.pojo.TestFileInfo;
import pers.wjx.ojsb.pojo.TryPassAmountPair;
import pers.wjx.ojsb.repository.ProblemRepository;
import pers.wjx.ojsb.repository.ProblemUserRepository;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Resource
    private ProblemRepository problemRepository;

    @Resource
    private ProblemUserRepository problemUserRepository;

    @Value("${test-location}")
    public String testLocation;

    @Override
    public ArrayList<ProblemEntry> getProblemEntriesByKey(String key, boolean byId, Integer pageIndex, Integer pageSize) {
        key = key.trim();
        ArrayList<ProblemEntry> problemEntries = new ArrayList<>();
        if (byId) {
            if (Pattern.matches("^\\d{1,8}$", key)) {
                Problem problem = problemRepository.getProblemById(Integer.valueOf(key));
                if (problem != null) {
                    problemEntries.add(new ProblemEntry(problem.getId(), problem.getName(), problem.getSubmit(), problem.getAccept()));
                }
            }
        } else {
            problemEntries = problemRepository.getProblemEntriesByName(key, (pageIndex - 1) * pageSize, pageSize);
        }
        return problemEntries;
    }

    @Override
    public Integer countProblemEntriesByKey(String key, boolean byId) {
        key = key.trim();
        if (byId) {
            if (Pattern.matches("^\\d{1,8}$", key) && problemRepository.getProblemById(Integer.valueOf(key)) != null) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return problemRepository.countProblemEntriesByName(key);
        }
    }

    @Override
    public ArrayList<ProblemEntry> getProblemEntriesByAuthorId(Integer authorId, Integer pageIndex, Integer pageSize) {
        return problemRepository.getProblemEntriesByAuthorId(authorId, (pageIndex - 1) * pageSize, pageSize);
    }

    @Override
    public Integer countProblemEntriesByAuthorId(Integer authorId) {
        return problemRepository.countProblemEntriesByAuthorId(authorId);
    }

    @Override
    public Problem getProblemById(Integer id) {
        return problemRepository.getProblemById(id);
    }

    @Override
    public Integer addProblem(Integer authorId, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit) {
        Problem problem = new Problem();
        problem.setAuthorId(authorId);
        problem.setName(name);
        problem.setDescription(description);
        problem.setInputFormat(inputFormat);
        problem.setOutputFormat(outputFormat);
        problem.setExplanation(explanation);
        problem.setSamples(samples);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);
        if (problemRepository.addProblem(problem)) {
            return problem.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean updateProblem(Integer id, String name, String description, String inputFormat, String outputFormat, String explanation, String samples, Integer timeLimit, Integer memoryLimit) {
        Problem problem = new Problem();
        problem.setId(id);
        problem.setName(name);
        problem.setDescription(description);
        problem.setInputFormat(inputFormat);
        problem.setOutputFormat(outputFormat);
        problem.setExplanation(explanation);
        problem.setSamples(samples);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);
        return problemRepository.updateProblem(problem);
    }

    @Override
    public Integer getAuthorIdById(Integer id) {
        return problemRepository.getAuthorIdById(id);
    }

    @Override
    public boolean deleteProblemById(Integer id) {
        return problemRepository.deleteProblemById(id);
    }

    @Override
    public boolean existProblem(Integer id) {
        return problemRepository.countProblemEntriesById(id) > 0;
    }

    @Override
    public TryPassAmountPair getTryPassAmountPairById(Integer id) {
        return new TryPassAmountPair(problemUserRepository.countTriedUserByProblemId(id), problemUserRepository.countPassedUserByProblemId(id));
    }

    @Override
    public TestFileInfo saveTestFile(Integer id, MultipartFile file) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream());
            ZipEntry zipEntry;
            Integer current = 0;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                // System.out.println(zipEntry.getName());
                current++;
                if (current > 32) {
                    return null;
                }
                if (current % 2 == 1) {
                    if (!zipEntry.getName().equals((current + 1) / 2 + ".in")) {
                        return null;
                    }
                } else {
                    if (!zipEntry.getName().equals(current / 2 + ".out")) {
                        return null;
                    }
                }
            }
            zipInputStream.close();
            if (current % 2 == 1) {
                return null;
            }
            File destFile = new File(testLocation + id + ".zip");
            if (!destFile.exists()) {
                file.transferTo(destFile.getAbsoluteFile());
                problemRepository.setTestSet(id);
            } else {
                file.transferTo(destFile.getAbsoluteFile());
            }
            return new TestFileInfo(id, current / 2, destFile.getName(), destFile.length(), new Date(destFile.lastModified()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteTestFile(Integer id) {
        File file = new File(testLocation + id + ".zip");
        return file.delete();
    }

    @Override
    public TestFileInfo getTestFileInfo(Integer id) {
        File file = new File(testLocation + id + ".zip");
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            Integer current = 0;
            while (zipInputStream.getNextEntry() != null) {
                current++;
            }
            zipInputStream.close();
            return new TestFileInfo(id, current / 2, file.getName(), file.length(), new Date(file.lastModified()));
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public FileSystemResource getTestFileResource(Integer id) {
        File file = new File(testLocation + id + ".zip");
        if(!file.exists()) {
            return null;
        }
        return new FileSystemResource(file);
    }
}
