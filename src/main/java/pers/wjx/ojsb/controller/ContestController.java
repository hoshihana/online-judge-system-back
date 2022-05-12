package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pers.wjx.ojsb.exception.*;
import pers.wjx.ojsb.pojo.*;
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
    @SaCheckRole("ADMIN")
    @PostMapping("")
    public Integer addContest(@Length(min = 1, max = 40, message = "比赛名称长度要在1到40之间") String name,
                              @NotNull(message = "比赛类型不能为空") ContestType type, String description,
                              @NotNull(message = "需明确是否设置密码") Boolean passwordSet, String password,
                              @NotNull(message = "开始时间不能为空") Date startTime,
                              @NotNull(message = "结束时间不能为空") Date endTime, Integer[] problemIds) {
        if (passwordSet) {
            if (password.length() < 6 || password.length() > 16) {
                throw new BadRequestException("密码长度需在6到16位之间");
            }
            if (!Pattern.matches("^[A-Za-z0-9]+$", password)) {
                throw new BadRequestException("密码只能包含数字或字母");
            }
        }
        startTime.setTime(startTime.getTime() - startTime.getTime() % (60 * 1000));
        endTime.setTime(endTime.getTime() - endTime.getTime() % (60 * 1000));
        Date current = new Date();
        if (startTime.getTime() - current.getTime() < 4 * 60 * 1000) {
            throw new BadRequestException("开始时间必须至少在当前时间的5分钟之后");
        }
        if (!endTime.after(startTime)) {
            throw new BadRequestException("结束时间必须在开始时间之后");
        }
        if (!contestService.validateProblemIds(StpUtil.getLoginIdAsInt(), new ArrayList<>(Arrays.asList(problemIds)))) {
            throw new BadRequestException("题目列表有重复题目或者非本人创建的题目");
        }
        Integer id = contestService.addContest(StpUtil.getLoginIdAsInt(), name, type, description, passwordSet, password, startTime, endTime);
        if (id == null) {
            throw new InternalServerErrorException("比赛创建失败");
        }
        if (contestService.setContestProblems(id, new ArrayList<>(Arrays.asList(problemIds)))) {
            return id;
        } else {
            throw new InternalServerErrorException("比赛题目配置失败");
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

    @GetMapping("/recent")
    public ArrayList<Contest> getRecentContests(@Range(min = 1, max = 7, message = "查询天数需在1到7天之间")Integer dayLimit) {
        return contestService.getRecentContests(dayLimit);
    }

    @SaCheckLogin
    @GetMapping("/{id}/permissions/enter")
    public String checkEnterContest(@PathVariable Integer id) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (!StpUtil.hasRole("ADMIN")) {
            Date current = new Date();
            if (current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法进入该比赛");
            }
            if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权进入该比赛");
            }
        }
        return "允许进入比赛";
    }

    @SaCheckLogin
    @GetMapping("")
    public ArrayList<Contest> getContestByKey(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                              @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                              @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        return contestService.getContestsByKey(key, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/amount")
    public Integer countContestsByKey(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key) {
        return contestService.countContestsByKey(key);
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/{id}/password")
    public String getContestPassword(@PathVariable Integer id) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (!contest.getPasswordSet()) {
            throw new BadRequestException("该比赛未设置密码");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权查看该比赛密码");
        }
        return contest.getPassword();
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/users/{authorId}")
    public ArrayList<Contest> getUserContestsByKey(@PathVariable Integer authorId,
                                                   @Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                                   @NotNull(message = "类型筛选不能为空") Boolean showPractice,
                                                   @NotNull(message = "类型筛选不能为空") Boolean showCompetition,
                                                   Boolean orderByStartTimeAsc,
                                                   @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                                   @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        if (authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return contestService.getUserContestsByKey(authorId, key, showPractice, showCompetition, orderByStartTimeAsc, pageIndex, pageSize);
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/users/{authorId}/amount")
    public Integer countUserContestsByKey(@PathVariable Integer authorId,
                                          @Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                          @NotNull(message = "类型筛选不能为空") Boolean showPractice,
                                          @NotNull(message = "类型筛选不能为空") Boolean showCompetition) {
        if (authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return contestService.countUserContestsByKey(authorId, key, showPractice, showCompetition);
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/{id}/permissions/edit")
    public String checkEditContest(@PathVariable Integer id) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权编辑该比赛");
        }
        return "允许编辑该比赛";
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PatchMapping("/{id}/detail")   // 比赛结束前均可以修改比赛详情
    public String updateContestDetail(@PathVariable Integer id,
                                      @Length(min = 1, max = 40, message = "比赛名称长度要在1到40之间") String name,
                                      @NotNull(message = "比赛类型不能为空") ContestType type, String description,
                                      @NotNull(message = "需明确是否设置密码") Boolean passwordSet, String password) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权编辑该比赛");
        }
        Date current = new Date();
        if (!current.before(contest.getEndTime())) {
            throw new BadRequestException("该比赛已结束，无法进行编辑");
        }
        if (passwordSet) {
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
    @SaCheckRole("ADMIN")
    @PatchMapping("/{id}/time") // 修改比赛时间（比赛开始前可用）
    public String updateContestTime(@PathVariable Integer id,
                                    @NotNull(message = "开始时间不能为空") Date startTime,
                                    @NotNull(message = "结束时间不能为空") Date endTime) {
        startTime.setTime(startTime.getTime() - startTime.getTime() % (60 * 1000));
        endTime.setTime(endTime.getTime() - endTime.getTime() % (60 * 1000));
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权修改比赛时间");
        }
        Date current = new Date();
        if (!current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛已开始或已结束，无法修改比赛时间");
        }
        if (startTime.getTime() - current.getTime() < 4 * 60 * 1000) {
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
    @SaCheckRole("ADMIN")
    @PostMapping("/{id}/reset") // 重置比赛（比赛进行时/结束后可用）
    public String resetContest(@PathVariable Integer id,
                               @NotNull(message = "开始时间不能为空") Date startTime,
                               @NotNull(message = "结束时间不能为空") Date endTime) {
        startTime.setTime(startTime.getTime() - startTime.getTime() % (60 * 1000));
        endTime.setTime(endTime.getTime() - endTime.getTime() % (60 * 1000));
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权重置比赛");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛尚未开始，无法重置比赛");
        }
        if (startTime.getTime() - current.getTime() < 4 * 60 * 1000) {
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
    @SaCheckRole("ADMIN")
    @PatchMapping("/{id}/endTime")  // 修改比赛结束时间（比赛进行时/结束后可用）
    public String updateContestEndTime(@PathVariable Integer id, @NotNull(message = "结束时间不能为空") Date endTime) {
        endTime.setTime(endTime.getTime() - endTime.getTime() % (60 * 1000));
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权修改比赛结束时间");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛尚未开始，无法修改比赛结束时间");
        }
        if (endTime.getTime() - current.getTime() < 4 * 60 * 1000) {
            throw new BadRequestException("结束时间必须至少在当前时间的5分钟之后");
        }
        if (contestService.setContestEndTime(id, endTime)) {
            return "比赛结束时间修改成功";
        } else {
            throw new InternalServerErrorException("比赛结束时间修改失败");
        }
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/{id}/problems")
    public String setContestProblems(@PathVariable Integer id, Integer[] problemIds) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (contest.getAuthorId() != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("无权设置该比赛题目列表");
        }
        Date current = new Date();
        if (!current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛已开始或已结束，无法设置题目列表");
        }
        if (!contestService.validateProblemIds(StpUtil.getLoginIdAsInt(), new ArrayList<>(Arrays.asList(problemIds)))) {
            throw new BadRequestException("题目列表有重复题目或者非本人创建的题目");
        }
        if (contestService.setContestProblems(id, new ArrayList<>(Arrays.asList(problemIds)))) {
            return "题目列表设置成功";
        } else {
            throw new InternalServerErrorException("题目列表设置失败");
        }
    }

    @SaCheckLogin
    @SaCheckRole("USER")
    @PostMapping("/{id}/participate")
    public String participateContest(@PathVariable Integer id, String nickname, String password) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new BadRequestException("比赛尚未开始");
        }
        if (nickname != null && nickname.length() > 30) {
            throw new BadRequestException("参赛昵称长度不能超过30");
        }
        if (contestService.participateContest(id, StpUtil.getLoginIdAsInt(), nickname, password)) {
            return "比赛参加成功";
        } else {
            throw new UnauthorizedException("参赛密码错误");
        }
    }

    @SaCheckLogin
    @PostMapping("/{id}/users/{userId}/isParticipant")
    public Boolean isParticipant(@PathVariable Integer id, @PathVariable Integer userId) {
        return contestService.isContestParticipant(id, userId);
    }

    @SaCheckLogin
    @GetMapping("/{id}/problemEntries") // 返回该比赛所设置的题目列表（按设置时的编号顺序），即使某个题目本身已被删除
    public ArrayList<ProblemEntry> getContestProblemEntries(@PathVariable Integer id) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (!StpUtil.hasRole("ADMIN")) {
            if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该比赛题目列表");
            }
            Date current = new Date();
            if (current.before(contest.getStartTime())) {
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
        if (problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if (!StpUtil.hasRole("ADMIN")) {
            if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该比赛题目");
            }
            Date current = new Date();
            if (current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法查看该比赛题目");
            }
            if (problem.getVisibility() == Visibility.PRIVATE) {
                throw new ForbiddenException("该题仅管理员可见");
            }
        }
        return problem;
    }

    @SaCheckLogin
    @GetMapping("/{id}/problems/{problemNumber}/status")
    public TryPassAmountPair getContestProblemTryPassAmount(@PathVariable Integer id, @PathVariable Integer problemNumber) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Problem problem = contestService.getContestProblem(id, problemNumber);
        if (problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if (!StpUtil.hasRole("ADMIN")) {
            if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该比赛题目解答情况");
            }
            Date current = new Date();
            if (current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法查看该比赛题目解答情况");
            }
            if (problem.getVisibility() == Visibility.PRIVATE) {
                throw new ForbiddenException("该题仅管理员可见");
            }
        }
        return contestService.getContestTryPassAmountPair(id, problemNumber);
    }

    @SaCheckLogin
    @GetMapping("/{id}/users/{userId}/status")
    public ArrayList<ContestProblemUserRelation> getContestProblemUserRelations(@PathVariable Integer id, @PathVariable Integer userId) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (!StpUtil.hasRole("ADMIN")) {
            if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该参赛用户解答情况");
            }
            Date current = new Date();
            if (current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法查看该参赛用户解答情况");
            }
        }
        if(!contestService.isContestParticipant(id, userId)) {
            throw new BadRequestException("该用户未参加比赛，无法查看其解答情况");
        }
        return contestService.getContestProblemUserRelations(id, userId);
    }

    @SaCheckLogin
    @GetMapping("/{id}/problems/{problemNumber}/users/{userId}")
    public ContestProblemUserRelation getContestProblemUserRelation(@PathVariable Integer id, @PathVariable Integer problemNumber, @PathVariable Integer userId) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Problem problem = contestService.getContestProblem(id, problemNumber);
        if (problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if (!StpUtil.getRoleList().contains("ADMIN")) {
            if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
                throw new ForbiddenException("未参加比赛，无权查看该参赛用户在该题的解答情况");
            }
            Date current = new Date();
            if (current.before(contest.getStartTime())) {
                throw new ForbiddenException("比赛尚未开始，无法查看该参赛用户在该题的解答情况");
            }
            if (problem.getVisibility() == Visibility.PRIVATE) {
                throw new ForbiddenException("该题仅管理员可见");
            }
        }
        return contestService.getContestProblemUserRelation(id, problemNumber, userId);
    }

    @SaCheckLogin
    @SaCheckRole("USER")
    @PostMapping("/{id}/records")
    public Integer addContestRecord(@PathVariable Integer id,
                                    @NotNull(message = "题目序号不能为空") Integer problemNumber,
                                    @NotNull(message = "请选择提交语言") Language submitLanguage,
                                    @NotBlank(message = "提交代码不能为空") String code) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Problem problem = contestService.getContestProblem(id, problemNumber);
        if (problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
            throw new ForbiddenException("未参加比赛，无权提交代码");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new ForbiddenException("比赛尚未开始，无法提交代码");
        }
        if (problem.getVisibility() == Visibility.PRIVATE) {
            throw new ForbiddenException("该题仅管理员可以提交代码");
        }
        if (!problem.getTestSet()) {
            throw new BadRequestException("该题目尚未配置测试点");
        }
        if (code.getBytes(StandardCharsets.UTF_8).length > maxCodeLength) {
            throw new BadRequestException("代码长度过长，不能超过" + maxCodeLength + "字节");
        }
        Integer recordId = contestService.submitCode(id, problemNumber, problem, StpUtil.getLoginIdAsInt(), submitLanguage, code);
        if (recordId == null) {
            throw new InternalServerErrorException("提交失败");
        } else {
            return recordId;
        }
    }

    @SaCheckLogin
    @SaCheckRole("USER")
    @GetMapping("/{id}/problems/{problemNumber}/records/recent")
    public ArrayList<Record> getContestProblemRecentRecord(@PathVariable Integer id, @PathVariable Integer problemNumber, @Min(value = 0, message = "返回记录条数必须为非负数") Integer limit) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Problem problem = contestService.getContestProblem(id, problemNumber);
        if (problem == null) {
            throw new NotFoundException("该题目不存在");
        }
        if (!contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
            throw new ForbiddenException("未参加比赛，无权查看该题目最近提交");
        }
        Date current = new Date();
        if (current.before(contest.getStartTime())) {
            throw new ForbiddenException("比赛尚未开始，无法查看该题目最近提交");
        }
        if (problem.getVisibility() == Visibility.PRIVATE) {
            throw new ForbiddenException("该题仅管理员可见");
        }
        return contestService.getContestProblemRecentRecords(id, problemNumber, StpUtil.getLoginIdAsInt(), limit);
    }

    @SaCheckLogin
    @GetMapping("/{id}/records")
    public ArrayList<Record> getContestRecords(@PathVariable Integer id, String problemNumber, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult, String orderBy, Boolean asc,
                                               @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                               @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (!StpUtil.hasRole("ADMIN") && !contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
            throw new ForbiddenException("仅参赛者和管理员可以查看该比赛的记录");
        }
        if (onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return contestService.getContestRecords(id, problemNumber, username, submitLanguage, judgeResult, orderBy, asc, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/{id}/records/amount")
    public Integer countContestRecords(@PathVariable Integer id, String problemNumber, String username, Boolean onlySelf, Language submitLanguage, JudgeResult judgeResult) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        if (!StpUtil.hasRole("ADMIN") && !contestService.isContestParticipant(id, StpUtil.getLoginIdAsInt())) {
            throw new ForbiddenException("无权查看该比赛的记录");
        }
        if (onlySelf) {
            username = (String) StpUtil.getSession().getAttribute("username");
        }
        return contestService.countContestRecords(id, problemNumber, username, submitLanguage, judgeResult);
    }

    @SaCheckLogin
    @GetMapping("/{id}/records/{recordId}")
    public Record getContestRecord(@PathVariable Integer id, @PathVariable Integer recordId) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Record record = contestService.getContestRecord(id, recordId);
        if (record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if (record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该记录仅个人可见，仅提交者和管理员可以查看该记录");
        }
        Date current = new Date();
        if (current.before(contest.getEndTime()) && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该比赛尚未结束，仅提交者和管理员可以查看该记录");
        }
        return record;
    }

    @SaCheckLogin
    @GetMapping("/{id}/records/{recordId}/code")
    public String getContestCode(@PathVariable Integer id, @PathVariable Integer recordId, Language submitLanguage, Integer codeLength) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Record record = contestService.getContestRecord(id, recordId);
        if (record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if (record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该记录记录仅个人可见，仅提交者和管理员可以查看该记录代码");
        }
        Date current = new Date();
        if (current.before(contest.getEndTime()) && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该比赛尚未结束，仅提交者和管理员可以查看该记录代码");
        }
        return recordService.getCode(recordId, submitLanguage, codeLength);
    }

    @SaCheckLogin
    @GetMapping("/{id}/records/{recordId}/compileOutput")
    public String getContestCompileOutput(@PathVariable Integer id, @PathVariable Integer recordId) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Record record = contestService.getContestRecord(id, recordId);
        if (record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if (record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该记录记录仅个人可见，仅提交者和管理员可以查看该编译信息");
        }
        Date current = new Date();
        if (current.before(contest.getEndTime()) && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该比赛尚未结束，仅提交者和管理员可以查看该编译信息");
        }
        return record.getCompileOutput();
    }

    @SaCheckLogin
    @GetMapping("/{id}/records/{recordId}/permissions/get")
    public String checkContestRecord(@PathVariable Integer id, @PathVariable Integer recordId) {
        Contest contest = contestService.getContestById(id);
        if (contest == null) {
            throw new NotFoundException("该比赛不存在");
        }
        Record record = contestService.getContestRecord(id, recordId);
        if (record == null) {
            throw new NotFoundException("该记录不存在");
        }
        if (record.getPersonal() && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该记录仅个人可见，仅提交者和管理员可以查看该记录");
        }
        Date current = new Date();
        if (current.before(contest.getEndTime()) && StpUtil.getLoginIdAsInt() != record.getUserId() && !StpUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("该比赛尚未结束，仅提交者和管理员可以查看该记录");
        }
        return "允许查看该记录";
    }
}
