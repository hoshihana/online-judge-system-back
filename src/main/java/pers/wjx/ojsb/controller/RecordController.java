package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
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

    @SaCheckLogin
    @PostMapping("")
    public Integer addRecord(@NotNull(message = "提交者id不能为空") Integer userId,
                             @NotNull(message = "提交题号不能为空") Integer problemId,
                             @NotNull(message = "请选择提交语言") Language submitLanguage,
                             @NotBlank(message = "提交代码不能为空") String code) {
        if (!accountService.existAccount(userId)) {
            throw new BadRequestException("提交至id不存在");
        }
        if (!problemService.existProblem(problemId)) {
            throw new BadRequestException("提交题号不存在");
        }
        Integer id = recordService.addRecord(userId, problemId, submitLanguage, code);
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
    public Record getRecords(@PathVariable Integer id) {
        return recordService.getRecordById(id);
    }

    @SaCheckLogin
    @GetMapping("/{id}/code")
    public String getCode(@PathVariable Integer id, Language submitLanguage, Integer codeLength) {
        return recordService.getCode(id, submitLanguage, codeLength);
    }

    @SaCheckLogin
    @GetMapping("/recent")
    public ArrayList<Record> getRecentRecords(Integer problemId, Integer userId, @Min(value = 0, message = "返回记录条数必须为非负数") Integer limit) {
        return recordService.getRecentRecords(problemId, userId, limit);
    }
}
