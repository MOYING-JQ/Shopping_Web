package com.moying.project_test.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moying.project_test.dto.*;
import com.moying.project_test.entity.User;
import com.baomidou.mybatisplus.spring.service.IService;
import com.moying.project_test.vo.CustomerVo;
import com.moying.project_test.vo.InfoVo;
import com.moying.project_test.vo.LoginVo;
import com.moying.project_test.vo.ProfileVo;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface IUserService extends IService<User> {


    LoginVo login(UserDto userDto);

    Boolean register(RegisterDto registerDto);

    InfoVo infoAll(String username);

    LoginVo refreshToken(RefreshDto refreshDto);

    Long findIdByName(String username);

    Byte findRoleByName(String username);

    ProfileVo change(ProfileDto profileDto);

    Boolean password(PasswordDTO passwordDTO);

    IPage<CustomerVo> pageAll(CustomerDto customerDto);
}
