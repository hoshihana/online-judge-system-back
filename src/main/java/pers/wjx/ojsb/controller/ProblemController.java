package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.ForbiddenException;
import pers.wjx.ojsb.exception.NotFoundException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/problems")
@Validated
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @SaCheckLogin
    @PostMapping("")    // 创建成功返回题目id
    public Integer addProblem(@Length(min = 1, max = 40, message = "题目名长度要在1到40之间") String name,
                              String description, String inputFormat, String outputFormat, String explanation, String samples,
                              @NotNull(message = "时间限制不能为空") @Min(value = 500, message = "时间限制不能少于500ms") Integer timeLimit,
                              @NotNull(message = "内存限制不能为空") @Min(value = 128, message = "内存限制不能少于128MB") Integer memoryLimit) {
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
                                 @NotNull(message = "时间限制不能为空") @Min(value = 500, message = "时间限制不能少于500ms") Integer timeLimit,
                                 @NotNull(message = "内存限制不能为空") @Min(value = 128, message = "内存限制不能少于128MB") Integer memoryLimit) {
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
}
