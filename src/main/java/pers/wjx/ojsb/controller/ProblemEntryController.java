package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.wjx.ojsb.exception.ForbiddenException;
import pers.wjx.ojsb.pojo.ProblemEntry;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@RestController
@RequestMapping("/problemEntries")
@Validated
public class ProblemEntryController {

    @Resource
    private ProblemService problemService;

    @SaCheckLogin
    @GetMapping("/public")
    public ArrayList<ProblemEntry> getPublicProblemEntriesByKey(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                                                @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                                                @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        return problemService.getPublicProblemEntriesByKey(key == null ? "" : key, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/public/amount")
    public Integer countPublicProblemEntriesByKey(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key) {
        return problemService.countProblemEntriesByKey(key == null ? "" : key);
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/user/{authorId}/all")
    public ArrayList<ProblemEntry> getAllUserProblemEntriesByKey(@PathVariable Integer authorId,
                                                              @Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                                              @NotNull(message = "状态筛选不能为空") Boolean showPrivate,
                                                              @NotNull(message = "状态筛选不能为空") Boolean showHidden,
                                                              @NotNull(message = "状态筛选不能为空") Boolean showPublic) {
        if (authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return problemService.getAllUserProblemEntriesByKey(authorId, key == null ? "" : key, showPrivate, showHidden, showPublic);
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/user/{authorId}")
    public ArrayList<ProblemEntry> getUserProblemEntriesByKey(@PathVariable Integer authorId,
                                                              @Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                                              @NotNull(message = "状态筛选不能为空") Boolean showPrivate,
                                                              @NotNull(message = "状态筛选不能为空") Boolean showHidden,
                                                              @NotNull(message = "状态筛选不能为空") Boolean showPublic,
                                                              @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                                              @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        if (authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return problemService.getUserProblemEntriesByKey(authorId, key == null ? "" : key, showPrivate, showHidden, showPublic, pageIndex, pageSize);
    }

    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/user/{authorId}/amount")
    public Integer countUserProblemEntriesByKey(@PathVariable Integer authorId,
                                                @Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                                @NotNull(message = "状态筛选不能为空") Boolean showPrivate,
                                                @NotNull(message = "状态筛选不能为空") Boolean showHidden,
                                                @NotNull(message = "状态筛选不能为空") Boolean showPublic) {
        if (authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return problemService.countUserProblemEntriesByKey(authorId, key == null ? "" : key, showPrivate, showHidden, showPublic);
    }
}
