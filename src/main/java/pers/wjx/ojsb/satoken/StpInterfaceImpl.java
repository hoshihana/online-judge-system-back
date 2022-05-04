package pers.wjx.ojsb.satoken;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;
import pers.wjx.ojsb.service.AccountService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    AccountService accountService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return null;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();
        roleList.add(accountService.getRoleById(Integer.parseInt((String) loginId)).name());
        return roleList;
    }
}
