package com.moying.project_test.util;

import com.moying.project_test.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author 墨莹
 * @date 2026/9/5 13:05
 */

@Component
public class FromJwt {


    public String username() {
        //是否是卖家
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 获取用户名
        assert authentication != null;
        String username = authentication.getName();
        return username;
    }
}
