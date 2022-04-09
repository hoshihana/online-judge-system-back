package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.exception.ForbiddenException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.exception.NotFoundException;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.pojo.enumeration.Visibility;
import pers.wjx.ojsb.service.ProblemService;
import pers.wjx.ojsb.service.RecordService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@RestController
@RequestMapping("/records")
@Validated
public class RecordController {

    @Resource
    private ProblemService problemService;

    @Resource
    private RecordService recordService;

    @Value("${max-code-length}")
    private Integer maxCodeLength;

    @SaCheckLogin
    @PostMapping("")
    public Integer addRecord(@NotNull(message = "提交题号不能为空") Integer problemId,
                             @NotNull(message = "请选择提交语言") Language submitLanguage,
                             @NotBlank(message = "提交代码不能为空") String code) {
        Problem problem = problemService.getProblemById(problemId);
        if (problem == null) {
            throw new BadRequestException("提交题号不存在");
        }
        if(problem.getVisibility() != Visibility.PUBLIC && problem.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new BadRequestException("无权提交代码");
        }
        if(!problem.getTestSet()) {
            throw new BadRequestException("该题目尚未配置测试点");
        }
        if(code.getBytes(StandardCharsets.UTF_8).length > maxCodeLength) {
            throw new BadRequestException("代码长度过长，不能超过" + maxCodeLength + "字节");
        }
        Integer id = recordService.addRecord(StpUtil.getLoginIdAsInt(), problemId , null, null, problem.getVisibility() != Visibility.PUBLIC, submitLanguage, code);
        if (id == null) {
            throw new InternalServerErrorException("提交失败");
        } else {
            return id;
        }
    }

    @SaCheckLogin
    @GetMapping("")
    public ArrayList<Record> getRecords(String problemId, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc,
                                        @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                        @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        if(onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return recordService.getRecords(problemId, username, submitLanguage, judgeResult, orderBy, asc, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/amount")
    public Integer countRecords(String problemId, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult) {
        if(onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return recordService.countRecords(problemId, username, submitLanguage, judgeResult);
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Record getRecord(@PathVariable Integer id) {
        Record record = recordService.getRecord(id);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if(record.getContestId() != null) {
            throw new ForbiddenException("比赛中提交记录无法直接查看");
        }
        if(record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId()) {   // todo 管理员可查看该记录
            throw new ForbiddenException("该题目为私密或赛题，仅提交者和管理员可以查看该记录");
        }
        return record;
    }

    @SaCheckLogin
    @GetMapping("/{id}/code")
    public String getCode(@PathVariable Integer id, Language submitLanguage, Integer codeLength) {
        Record record = recordService.getRecord(id);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if(record.getContestId() != null) {
            throw new ForbiddenException("比赛中提交的代码无法直接查看");
        }
        if(record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId()) {   // todo 管理员可查看该提交代码
            throw new ForbiddenException("该题目为私密或赛题，仅提交者和管理员可以查看该提交代码");
        }
        return recordService.getCode(id, submitLanguage, codeLength);
    }

    @SaCheckLogin
    @GetMapping("/{id}/compileOutput")
    public String getCompileOutput(@PathVariable Integer id) {
        Record record = recordService.getRecord(id);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if(record.getContestId() != null) {
            throw new ForbiddenException("比赛中的编译信息无法直接查看");
        }
        if(record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId()) {   // todo 管理员可查看该编译信息
            throw new ForbiddenException("该题目为私密或赛题，仅提交者和管理员可以查看该编译信息");
        }
        if(record.getCompileOutput() == null) {
            throw new NotFoundException("无编译信息");
        } else {
            return record.getCompileOutput();
        }
    }

    @SaCheckLogin
    @GetMapping("/recent")
    public ArrayList<Record> getRecentRecordsByProblemId(Integer problemId, @Min(value = 0, message = "返回记录条数必须为非负数") Integer limit) {
        Problem problem = problemService.getProblemById(problemId);
        if(problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if(problem.getVisibility() != Visibility.PUBLIC && problem.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new BadRequestException("无权查看该题我的最近提交");
        }
        return recordService.getRecentRecords(problemId, StpUtil.getLoginIdAsInt(), limit);
    }
}
