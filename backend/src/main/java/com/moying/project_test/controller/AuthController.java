package com.moying.project_test.controller;

import com.moying.project_test.common.Result;
import com.moying.project_test.dto.*;
import com.moying.project_test.service.IUserService;

import com.moying.project_test.vo.InfoVo;
import com.moying.project_test.vo.LoginVo;

import com.moying.project_test.vo.ProfileVo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 墨莹
 * @date 2026/8/25 11:53
 */


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;


    @PostMapping("/login")
    public Result<LoginVo> login(@Validated @RequestBody UserDto userDto) {
        LoginVo loginVo = userService.login(userDto);
        return Result.success(loginVo,"登陆成功");
    }


    @PostMapping("/register")
    public Result<Boolean> register(@Validated @RequestBody RegisterDto registerDto) {
        Boolean bool = userService.register(registerDto);
        return Result.success(bool,"注册成功");
    }


    @GetMapping("/info")
    public Result<InfoVo> info() {
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 获取用户名
        assert authentication != null;
        String username = authentication.getName();

        InfoVo infoVo = userService.infoAll(username);
        return Result.success(infoVo);
    }

    @PostMapping("/refresh")
    public Result<LoginVo> refresh(@RequestBody RefreshDto refreshDto) {

        System.out.println("dscvsdc"+refreshDto);
        LoginVo loginVo = userService.refreshToken(refreshDto);
        return Result.success(loginVo,"刷新成功");
    }

    @PostMapping
    public Result<Boolean> logout(@RequestBody RefreshDto refreshDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String username = authentication.getName();
        //使得refreshToken失效
        //
        //
        // 清空安全上下文
        SecurityContextHolder.clearContext();
        return Result.success(true,"登出成功");
    }

    @PutMapping("/profile")
    public Result<ProfileVo> profile(@RequestBody ProfileDto profileDto){

        ProfileVo profileVo = userService.change(profileDto);
        return Result.success(profileVo);
    }

    @PutMapping("/password")
    public Result<Boolean> password(@RequestBody PasswordDTO passwordDTO){

        Boolean bool = userService.password(passwordDTO);
        return bool?Result.success(bool):Result.fail(String.valueOf(bool));
    }




}
