package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.BadRequestException;
import pers.wjx.ojsb.exception.InternalServerErrorException;
import pers.wjx.ojsb.exception.NotFoundException;
import pers.wjx.ojsb.pojo.Contest;
import pers.wjx.ojsb.pojo.enumeration.ContestType;
import pers.wjx.ojsb.service.AccountService;
import pers.wjx.ojsb.service.ContestService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/contests")
@Validated
public class ContestController {

    @Resource
    private ContestService contestService;

    @Resource
    private AccountService accountService;

    @SaCheckLogin
    @PostMapping("")
    public Integer addContest(@Length(min = 1, max = 40, message = "比赛名称长度要在1到40之间") String name,
                              @NotNull(message = "比赛类型不能为空") ContestType type, String description, String password,
                              @NotNull(message = "开始时间不能为空") Date startTime,
                              @NotNull(message = "结束时间不能为空") Date endTime) {
        if (password != null) {
            if (password.length() < 6 || password.length() > 16) {
                throw new BadRequestException("密码长度需在6到16位之间");
            }
            if (!Pattern.matches("^[A-Za-z0-9]+$", password)) {
                throw new BadRequestException("密码只能包含数字或字母");
            }
        }
        Date current = new Date();
        if (startTime.getTime() - current.getTime() < (4 * 60 + 30) * 1000) {
            throw new BadRequestException("开始时间必须至少在当前时间的5分钟之后");
        }
        if (!endTime.after(startTime)) {
            throw new BadRequestException("结束时间必须在开始时间之后");
        }
        Integer id = contestService.addContest(StpUtil.getLoginIdAsInt(), name, type, description, password, startTime, endTime);
        if (id == null) {
            throw new InternalServerErrorException("比赛创建失败");
        } else {
            return id;
        }
    }

    @SaCheckLogin
    @PatchMapping("/{id}/detail")   // 比赛创建后任何阶段均能修改比赛详情
    public String updateContestDetail(@PathVariable Integer id,
                                      @Length(min = 1, max = 40, message = "比赛名称长度要在1到40之间") String name,
                                      @NotNull(message = "比赛类型不能为空") ContestType type, String description, String password) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (password != null) {
            if (password.length() < 6 || password.length() > 16) {
                throw new BadRequestException("密码长度需在6到16位之间");
            }
            if (!Pattern.matches("^[A-Za-z0-9]+$", password)) {
                throw new BadRequestException("密码只能包含数字或字母");
            }
        }
        if (contestService.updateContestDetail(id, name, type, description, password)) {
            return "比赛详情编辑成功";
        } else {
            throw new InternalServerErrorException("比赛详情编辑失败");
        }
    }

    @SaCheckLogin
    @PatchMapping("/{id}/time") // 修改比赛时间（比赛开始前可用）
    public String updateContestTime(@PathVariable Integer id,
                                    @NotNull(message = "开始时间不能为空") Date startTime,
                                    @NotNull(message = "结束时间不能为空") Date endTime) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Date current = new Date();
        if (!current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛已开始或已结束，无法修改比赛时间");
        }
        if (startTime.getTime() - current.getTime() < (4 * 60 + 30) * 1000) {
            throw new BadRequestException("开始时间必须至少在当前时间的5分钟之后");
        }
        if (!endTime.after(startTime)) {
            throw new BadRequestException("结束时间必须在开始时间之后");
        }
        if (contestService.setContestTime(id, startTime, endTime)) {
            return "比赛时间修改成功";
        } else {
            throw new InternalServerErrorException("比赛时间修改失败");
        }
    }

    @SaCheckLogin
    @PostMapping("/{id}/reset") // 重置比赛（比赛进行时/结束后可用）
    public String resetContest(@PathVariable Integer id,
                               @NotNull(message = "开始时间不能为空") Date startTime,
                               @NotNull(message = "结束时间不能为空") Date endTime) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛尚未开始，无法重置比赛");
        }
        if (startTime.getTime() - current.getTime() < (4 * 60 + 30) * 1000) {
            throw new BadRequestException("开始时间必须至少在当前时间的5分钟之后");
        }
        if (!endTime.after(startTime)) {
            throw new BadRequestException("结束时间必须在开始时间之后");
        }
        if (contestService.resetContest(id, startTime, endTime)) {
            return "比赛重置成功";
        } else {
            throw new InternalServerErrorException("比赛重置失败");
        }
    }

    @SaCheckLogin
    @PatchMapping("/{id}/endTime")  // 修改比赛结束时间（比赛进行时/结束后可用）
    public String updateContestEndTime(@PathVariable Integer id, @NotNull(message = "结束时间不能为空") Date endTime) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛尚未开始，无法修改比赛结束时间");
        }
        if (endTime.getTime() - current.getTime() < (4 * 60 + 30) * 1000) {
            throw new BadRequestException("结束时间必须至少在当前时间的5分钟之后");
        }
        if (contestService.setContestEndTime(id, endTime)) {
            return "比赛结束时间修改成功";
        } else {
            throw new InternalServerErrorException("比赛结束时间修改失败");
        }
    }

    @SaCheckLogin
    @PostMapping("/{id}/problems")
    public String setContestProblems(@PathVariable Integer id, Integer[] problemIds) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Date current = new Date();
        if (!current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛已开始或已结束，无法设置题目列表");
        }
        if (contestService.setContestProblems(id, (ArrayList<Integer>) Arrays.asList(problemIds))) {
            return "题目列表设置成功";
        } else {
            throw new InternalServerErrorException("题目列表设置失败");
        }
    }
}
