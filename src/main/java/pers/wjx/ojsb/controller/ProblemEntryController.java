package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
    public ArrayList<ProblemEntry> getProblemEntriesByKey(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                                         @NotNull(message = "查询方式不能为空") boolean byId,
                                                         @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                                         @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        return problemService.getProblemEntriesByKey(key == null ? "" : key, byId, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/public/amount")
    public Integer countProblemEntriesByKey(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                     @NotNull(message = "查询方式不能为空") boolean byId) {
        return problemService.countProblemEntriesByKey(key == null ? "" : key, byId);
    }

    @SaCheckLogin
    @GetMapping("/user/{authorId}")
    public ArrayList<ProblemEntry> getProblemEntriesByAuthorId(@PathVariable Integer authorId,
                                                               @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                                               @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        if(authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return problemService.getProblemEntriesByAuthorId(authorId, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/user/{authorId}/amount")
    public Integer countProblemEntriesByAuthorId(@PathVariable Integer authorId) {
        if(authorId != StpUtil.getLoginIdAsInt()) {
            throw new ForbiddenException("用户无权访问");
        }
        return problemService.countProblemEntriesByAuthorId(authorId);
    }
}
