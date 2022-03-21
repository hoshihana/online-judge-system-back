package pers.wjx.ojsb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.wjx.ojsb.pojo.ProblemBrief;
import pers.wjx.ojsb.service.ProblemService;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import java.util.ArrayList;

@RestController
@RequestMapping("/problem")
@Validated
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @SaCheckLogin
    @GetMapping("/list")
    public ArrayList<ProblemBrief> list(@Length(min = 0, max = 40, message = "搜索关键字长度要在0到40之间") String key,
                                         @Min(value = 1, message = "页码不能小于1") Integer pageIndex,
                                         @Min(value = 1, message = "页面大小不能小于1") Integer pageSize) {
        System.out.println(key + "-" + pageIndex + "-" + pageSize);
        return problemService.getProblemBriefByKey(key == null ? "" : key, pageIndex, pageSize);
    }
}
