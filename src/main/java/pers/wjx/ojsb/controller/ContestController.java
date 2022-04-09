package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.*;
import pers.wjx.ojsb.pojo.Contest;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemEntry;
import pers.wjx.ojsb.pojo.Record;
import pers.wjx.ojsb.pojo.enumeration.ContestType;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;
import pers.wjx.ojsb.pojo.enumeration.Visibility;
import pers.wjx.ojsb.service.ContestService;
import pers.wjx.ojsb.service.RecordService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
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
    private RecordService recordService;

    @Value("${max-code-length}")
    private Integer maxCodeLength;

    @SaCheckLogin
    @PostMapping("")
    public Integer addContest(@Length(min = 1, max = 40, message = "比赛名称长度要在1到40之间") String name,
                              @NotNull(message = "比赛类型不能为空") ContestType type, String description,
                              @NotNull(message = "需明确是否设置密码") Boolean passwordSet, String password,
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
        Integer id = contestService.addContest(StpUtil.getLoginIdAsInt(), name, type, description, passwordSet, password, startTime, endTime);
        if (id == null) {
            throw new InternalServerErrorException("比赛创建失败");
        } else {
            return id;
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Contest getContest(@PathVariable Integer id) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        return contest;
    }

    @SaCheckLogin
    @PatchMapping("/{id}/detail")   // 比赛创建后任何阶段均能修改比赛详情
    public String updateContestDetail(@PathVariable Integer id,
                                      @Length(min = 1, max = 40, message = "比赛名称长度要在1到40之间") String name,
                                      @NotNull(message = "比赛类型不能为空") ContestType type, String description,
                                      @NotNull(message = "需明确是否设置密码") Boolean passwordSet, String password) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权编辑该比赛");
        }
        if (password != null) {
            if (password.length() < 6 || password.length() > 16) {
                throw new BadRequestException("密码长度需在6到16位之间");
            }
            if (!Pattern.matches("^[A-Za-z0-9]+$", password)) {
                throw new BadRequestException("密码只能包含数字或字母");
            }
        }
        if (contestService.updateContestDetail(id, name, type, description, passwordSet, password)) {
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
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权修改比赛时间");
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
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权重置比赛");
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
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权修改比赛结束时间");
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
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权设置比赛题目列表");
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

    @SaCheckLogin
    @PostMapping("/{id}/participate")
    public String participateContest(@PathVariable Integer id, String nickname, String password) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if(contest.getAuthorId() == StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("比赛作者无法参加比赛");
        }
        if(contestService.participateContest(id, StpUtil.getLoginIdAsInt(), nickname, password)) {
            return "比赛参加成功";
        } else {
            throw new UnauthorizedException("比赛密码错误");
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}/problemEntries") // 返回该比赛所设置的题目列表（按设置时的编号顺序），即使某个题目本身已被删除
    public ArrayList<ProblemEntry> getContestProblemEntries(@PathVariable Integer id) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            if(!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该比赛题目列表");
            }
            Date current = new Date();
            if(current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法查看该比赛题目列表");
            }
        }
        return contestService.getContestProblemEntries(id);
    }

    @SaCheckLogin
    @GetMapping("/{id}/problems/{problemNumber}")
    public Problem getContestProblem(@PathVariable Integer id, @PathVariable Integer problemNumber) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Problem problem = contestService.getContestProblem(id, problemNumber);
        if(problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            if(!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该题目");
            }
            Date current = new Date();
            if(current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法查看该题目");
            }
            if(problem.getVisibility() == Visibility.PRIVATE) {
                throw new ForbiddenException("该题仅作者可见");
            }
        }
        return problem;
    }

    @SaCheckLogin
    @PostMapping("/{id}/records")
    public Integer addContestRecord(@PathVariable Integer id,
                                    @NotNull(message = "题目序号不能为空") Integer problemNumber,
                                    @NotNull(message = "请选择提交语言") Language submitLanguage,
                                    @NotBlank(message = "提交代码不能为空") String code) {
        Contest contest = contestService.getContestById(id);
        if(contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Problem problem = contestService.getContestProblem(id, problemNumber);
        if(problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if(contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            if(!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权提交代码");
            }
            Date current = new Date();
            if(current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法提交代码");
            }
            if(problem.getVisibility() == Visibility.PRIVATE) {
                throw new ForbiddenException("该题仅作者可提交代码");
            }
        }
        if(!problem.getTestSet()) {
            throw new BadRequestException("该题目尚未配置测试点");
        }
        if(code.getBytes(StandardCharsets.UTF_8).length > maxCodeLength) {
            throw new BadRequestException("代码长度过长，不能超过" + maxCodeLength + "字节");
        }
        Integer recordId = recordService.addRecord(StpUtil.getLoginIdAsInt(), problem.getId(), id, problemNumber, problem.getVisibility() == Visibility.PRIVATE, submitLanguage, code);
        if (recordId == null) {
            throw new InternalServerErrorException("提交失败");
        } else {
            return recordId;
        }
    }

    @SaCheckLogin
    @GetMapping("/{id}/records")
    public ArrayList<Record> getContestRecords(@PathVariable Integer id, String problemNumber, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc,
                                               @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                               @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        if(onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return contestService.getContestRecords(id, problemNumber, username, submitLanguage, judgeResult, orderBy, asc, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/{id}/amount")
    public Integer countContestRecords(@PathVariable Integer id, String problemNumber, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult) {
        if(onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return contestService.countContestRecords(id, problemNumber, username, submitLanguage, judgeResult);
    }

    @SaCheckLogin
    @GetMapping("/{id}/records/{recordId}")
    public Record getContestRecord(@PathVariable Integer id, @PathVariable Integer recordId) {
        Contest contest = getContest(id);
        if(contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Record record = contestService.getContestRecord(id, recordId);
        if(record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if(record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId()) {   // todo 管理员可查看该记录
            throw new ForbiddenException("该题目为私密，仅提交者和管理员可以查看该记录");
        }
        Date current = new Date();
        if(!current.after(contest.getEndTime()) && StpUtil.getLoginIdAsInt() != record.getUserId()) {
            throw new ForbiddenException("该比赛尚未结束，仅提交者和管理员可以查看该记录");
        }
        return record;
    }
}
