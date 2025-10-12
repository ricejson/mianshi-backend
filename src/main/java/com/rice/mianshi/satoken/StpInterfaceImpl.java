package com.rice.mianshi.satoken;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.rice.mianshi.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.rice.mianshi.constant.UserConstant.USER_LOGIN_STATE;

/**
 * StpInterface 实现类（如何获取某个用户的权限）
 * @author ricejosn
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object o, String s) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        if (o == null) {
            return new ArrayList<>();
        }
        Long loginId = (Long) o;
        // 获取当前登录用户
        User user = (User) StpUtil.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        return Collections.singletonList(user.getUserRole());
    }
}
