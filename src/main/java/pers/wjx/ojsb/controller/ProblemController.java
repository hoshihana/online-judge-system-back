package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.wjx.ojsb.exception.NotFoundException;
import pers.wjx.ojsb.pojo.Problem;
import pers.wjx.ojsb.pojo.ProblemBrief;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@RestController
@RequestMapping("/problem")
@Validated
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @SaCheckLogin
    @GetMapping("")
    public ArrayList<ProblemBrief> list(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                        @NotNull(message = "查询方式不能为空") boolean byId,
                                        @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                        @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        return problemService.getProblemBriefsByKey(key == null ? "" : key, byId, pageIndex, pageSize);
    }

    @SaCheckLogin
    @GetMapping("/amount")
    public Integer amount(@Length(max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                        @NotNull(message = "查询方式不能为空") boolean byId) {
        return problemService.countProblemBriefsByKey(key == null ? "" : key, byId);
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Problem amount(@PathVariable Integer id) {
        Problem problem = problemService.getProblemById(id);
        if(problem == null) {
            throw new NotFoundException("题号不存在");
        } else {
            return problem;
        }
    }
}
