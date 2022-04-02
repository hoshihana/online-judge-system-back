package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.exception.ForbiddenException;
import pers.wjx.ojsb.exception.NotFoundException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemUserRelation;
import pers.wjx.ojsb.pojo.TestFileInfo;
import pers.wjx.ojsb.pojo.TryPassAmountPair;
import pers.wjx.ojsb.service.ProblemService;
import pers.wjx.ojsb.service.ProblemUserService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/problems")
@Validated
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @Resource
    private ProblemUserService problemUserService;

    @SaCheckLogin
    @PostMapping("")    // 创建成功返回题目id
    public Integer addProblem(@Length(min = 1, max = 40, message = "题目名长度要在1到40之间") String name,
                              String description, String inputFormat, String outputFormat, String explanation, String samples,
                              @NotNull(message = "时间限制不能为空") @Range(min = 500, max = 15000, message = "时间限制必须在500ms到15000ms之间") Integer timeLimit,
                              @NotNull(message = "内存限制不能为空") @Range(min = 128, max = 512, message = "内存限制必须在128MB到512MB之间") Integer memoryLimit) {
        Integer id = problemService.addProblem(StpUtil.getLoginIdAsInt(), name, description, inputFormat, outputFormat, explanation, samples, timeLimit, memoryLimit);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerErrorException("题目创建失败");
        }
    }

    @SaCheckLogin
    @PatchMapping("/{id}")
    public Integer updateProblem(@PathVariable Integer id, @Length(min = 1, max = 40, message = "题目名长度要在1到40之间") String name,
                                 String description, String inputFormat, String outputFormat, String explanation, String samples,
                                 @NotNull(message = "时间限制不能为空") @Range(min = 500, max = 15000, message = "时间限制必须在500ms到15000ms之间") Integer timeLimit,
                                 @NotNull(message = "内存限制不能为空") @Range(min = 128, max = 512, message = "内存限制必须在128MB到512MB之间") Integer memoryLimit) {
        if (problemService.getAuthorIdById(id) != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权编辑该题目");
        }
        if (problemService.updateProblem(id, name, description, inputFormat, outputFormat, explanation, samples, timeLimit, memoryLimit)) {
            return id;
        } else {
            throw new InternalServerErrorException("题目编辑失败");
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Problem getProblemById(@PathVariable Integer id) {
        Problem problem = problemService.getProblemById(id);
        if (problem == null) {
            throw new NotFoundException("题号不存在");
        } else {
            return problem;
        }
    }

    @SaCheckLogin
    @DeleteMapping("/{id}")
    public Integer deleteProblemById(@PathVariable Integer id) {
        Integer authorId = problemService.getAuthorIdById(id);
        if (authorId == null) {
            throw new NotFoundException("题号不存在");
        } else if (StpUtil.getLoginIdAsInt() != authorId) {
            throw new ForbiddenException("无权删除该题目");
        } else if (problemService.deleteProblemById(id)) {
            problemService.deleteTestFile(id);
            return id;
        } else {
            throw new InternalServerErrorException("题目删除失败");
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}/authorId")
    public Integer getAuthorIdById(@PathVariable Integer id) {
        Integer authorId = problemService.getAuthorIdById(id);
        if (authorId == null) {
            throw new NotFoundException("题号不存在");
        } else {
            return authorId;
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}/amount")
    public TryPassAmountPair getTryPassAmountById(@PathVariable Integer id) {
        return problemService.getTryPassAmountPairById(id);
    }

    @SaCheckLogin
    @GetMapping("/{problemId}/users/{userId}")
    public ProblemUserRelation getProblemUserRelation(@PathVariable Integer userId, @PathVariable Integer problemId) {
        return problemUserService.getProblemUserRelation(userId, problemId);
    }

    @SaCheckLogin
    @PostMapping("/{id}/test")
    public TestFileInfo addProblemTestFile(@PathVariable Integer id, @NotNull(message = "上传测试点文件不能为空") MultipartFile file) {
        Integer authorId = problemService.getAuthorIdById(id);
        if (authorId == null) {
            throw new NotFoundException("题号不存在");
        }
        if (StpUtil.getLoginIdAsInt() != authorId) {
            throw new ForbiddenException("无权配置该题目测试点");
        }
        if (!file.getContentType().equals("application/x-zip-compressed")) {
            throw new BadRequestException("测试点文件格式错误");
        }
        if (file.isEmpty()) {
            throw new BadRequestException("测试点文件中无内容");
        }
        if (file.getSize() > 30 * 1024 * 1024) {
            throw new BadRequestException("测试点文件大小超过30MB");
        }
        TestFileInfo testFileInfo = problemService.saveTestFile(id, file);
        if (testFileInfo == null) {
            throw new BadRequestException("测试点文件内部格式有误，请严格遵循上传要求");
        } else {
            return testFileInfo;
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}/test")
    public ResponseEntity<FileSystemResource> getProblemTest(@PathVariable Integer id) {
        Integer authorId = problemService.getAuthorIdById(id);
        if (authorId == null) {
            throw new NotFoundException("题号不存在");
        }
        if (StpUtil.getLoginIdAsInt() != authorId) {
            throw new ForbiddenException("无权下载该题测试点文件");
        }
        FileSystemResource resource = problemService.getTestFileResource(id);
        if (resource == null) {
            throw new NotFoundException("该题目尚未配置数据点");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", id + ".zip");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @SaCheckLogin
    @GetMapping("/{id}/testInfo")
    public TestFileInfo getProblemTestInfo(@PathVariable Integer id) {
        Integer authorId = problemService.getAuthorIdById(id);
        if (authorId == null) {
            throw new NotFoundException("题号不存在");
        }
        if (StpUtil.getLoginIdAsInt() != authorId) {
            throw new ForbiddenException("无权查看该题测试点信息");
        }
        TestFileInfo testFileInfo = problemService.getTestFileInfo(id);
        if (testFileInfo == null) {
            throw new NotFoundException("该题目尚未配置测试点");
        } else {
            return testFileInfo;
        }
    }
}

