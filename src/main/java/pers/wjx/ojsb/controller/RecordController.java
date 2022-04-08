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
import pers.wjx.ojsb.service.AccountService;
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
    private AccountService accountService;

    @Resource
    private ProblemService problemService;

    @Resource
    private RecordService recordService;

    @Value("${max-code-length}")
    private Integer maxCodeLength;

    @SaCheckLogin
    @PostMapping("")
    public Integer addRecord(@NotNull(message = "提交题号不能为空") Integer problemId,
                             Integer contestId, // todo 对contestId进行校验
                             @NotNull(message = "请选择提交语言") Language submitLanguage,
                             @NotBlank(message = "提交代码不能为空") String code) {
        Problem problem = problemService.getProblemById(problemId);
        if (problem == null) {
            throw new BadRequestException("提交题号不存在");
        }
        if(!problem.getTestSet()) {
            throw new BadRequestException("改题目尚未配置测试点");
        }
        if(code.getBytes(StandardCharsets.UTF_8).length > maxCodeLength) {
            throw new BadRequestException("代码长度过长，不能超过" + maxCodeLength + "字节");
        }
        Integer id = recordService.addRecord(StpUtil.getLoginIdAsInt(), problemId, contestId, problem.getVisibility(), submitLanguage, code);
        if (id == null) {
            throw new InternalServerErrorException("提交失败");
        } else {
            return id;
        }
    }

    @SaCheckLogin
    @GetMapping("")
    public ArrayList<Record> getPublicRecords(String problemId, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc,
                                        @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                        @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        if(onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return recordService.getPublicRecords(problemId, username, submitLanguage, judgeResult, orderBy, asc, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/amount")
    public Integer countPublicRecords(String problemId, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult) {
        if(onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return recordService.countPublicRecords(problemId, username, submitLanguage, judgeResult);
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Record getRecord(@PathVariable Integer id) {
        Record record = recordService.getRecordById(id);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        } else {
            return record;
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}/code")
    public String getCode(@PathVariable Integer id, Language submitLanguage, Integer codeLength) {
        Record record = recordService.getRecordById(id);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        }
        return recordService.getCode(id, submitLanguage, codeLength);
    }

    @SaCheckLogin
    @GetMapping("/{id}/compileOutput")
    public String getCompileOutput(@PathVariable Integer id) {
        Record record = recordService.getRecordById(id);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if(record.getCompileOutput() == null) {
            throw new NotFoundException("无编译信息");
        } else {
            return record.getCompileOutput();
        }
    }

    @SaCheckLogin
    @GetMapping("/recent")
    public ArrayList<Record> getRecentRecords(Integer problemId, Integer userId, @Min(value = 0, message = "返回记录条数必须为非负数") Integer limit) {
        if(StpUtil.getLoginIdAsInt() != userId) {
            throw new ForbiddenException("无权获取该用户最近提交记录");
        }
        return recordService.getRecentRecords(problemId, userId, limit);
    }
}
