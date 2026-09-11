package com.moying.project_test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.dto.*;
import com.moying.project_test.entity.OrderStat;
import com.moying.project_test.entity.User;
import com.moying.project_test.exception.BusinessException;
import com.moying.project_test.mapper.UserMapper;
import com.moying.project_test.service.IOrderService;
import com.moying.project_test.service.IUserService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.moying.project_test.util.FromJwt;
import com.moying.project_test.util.JwtUtil;
import com.moying.project_test.vo.CustomerVo;
import com.moying.project_test.vo.InfoVo;
import com.moying.project_test.vo.LoginVo;
import com.moying.project_test.vo.ProfileVo;
import io.jsonwebtoken.Claims;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;


    private final JwtUtil jwtUtil;

    @Autowired
    private FromJwt fromJwt;


    public UserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVo login(UserDto userDto) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userDto.getUsername());

        //获取数据库user对象
        User user = this.getOne(queryWrapper,false);

        //验证
        if(user==null){
            throw new BusinessException(400,"用户信息为空");
        }

        // BCrypt密码校验
        boolean ok = passwordEncoder.matches(userDto.getPassword(),user.getPassword());

        if(!ok){
            throw new BusinessException(400,"密码错误");
        }

        //分配token
        String accessToken = jwtUtil.getAccessToken(userDto.getUsername());
        String refreshToken = jwtUtil.getRefreshToken(userDto.getUsername());

        //封装响应对象

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(accessToken);
        loginVo.setRefreshToken(refreshToken);
        return loginVo;
    }


    @Override
    public Boolean register(RegisterDto registerDto) {


        //验证是否存在username
        //提前校验用户名是否已经存在，避免数据库抛唯一索引异常
        boolean exists = lambdaQuery()
                .eq(User::getUsername, registerDto.getUsername())
                .exists();
        if (exists) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setPhone(registerDto.getPhone());
        user.setRole(registerDto.getRole());

        boolean bool = save(user);
        if(bool){
            return true;
        }else {
            throw new BusinessException(400,"注册失败");
        }


    }

    @Override
    public InfoVo infoAll(String username) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        User user = this.getOne(queryWrapper,false);
        if(user==null){
            throw new BusinessException(400,"暂无信息");
        }

        InfoVo infoVo = new InfoVo();
        infoVo.setId(user.getId());
        infoVo.setUsername(user.getUsername());
        infoVo.setEmail(user.getEmail());
        infoVo.setPhone(user.getPhone());
        infoVo.setRole(user.getRole());
        infoVo.setAvatar(user.getAvatar());
        infoVo.setNickname(user.getNickname());
        infoVo.setRole(user.getRole());
        infoVo.setLevel(user.getLevel());
        infoVo.setStatus(user.getStatus());
        infoVo.setCreateTime(user.getCreateTime());
        infoVo.setUpdateTime(user.getUpdateTime());
        infoVo.setRegisterTime(user.getRegisterTime());

        return infoVo;
    }

    @Override
    public LoginVo refreshToken(RefreshDto refreshDto) {
        String refreshToken = refreshDto.getRefreshToken();

        System.out.println("refreshToken:"+refreshToken);
        //验证token合法性
        Claims claims = jwtUtil.parseToken(refreshToken);
        String token_type = claims.get("token_type").toString();
        String username = claims.getSubject();
        boolean exists = lambdaQuery()
                .eq(User::getUsername,username)
                .exists();
        if (!exists) {
            throw new BusinessException(401,"非法token");
        }
        if (!"refresh_token".equals(token_type)) {
            throw new BusinessException(401,"token类型错误");
        }
        Long loginAt = (Long) claims.get("loginAt");
        long maxTotal = 7L *24*60*60*1000;
        if(System.currentTimeMillis() - loginAt > maxTotal){
            throw new BusinessException("会话已过期，请重新登录");
        }

        //生新的token
        String newAccessToken = jwtUtil.getAccessToken(username);
        String newRefreshToken = jwtUtil.refreshToken(claims);

        //使得refreshToken失效
        //
        //redis里面加入黑名单

        //分装
        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(newAccessToken);
        loginVo.setRefreshToken(newRefreshToken);

        return loginVo;

    }

    @Override
    public Long findIdByName(String username) {
        User user = this.getOne(new QueryWrapper<User>().eq("username",username));
        Long id = user.getId();
        return id;
    }

    @Override
    public Byte findRoleByName(String username) {
        User user = this.getOne(new QueryWrapper<User>().eq("username",username));
        Byte role = user.getRole();
        return role;
    }

    @Override
    public ProfileVo change(ProfileDto profileDto) {

        // 1. 先从数据库查出当前登录用户原始数据
        Long id = findIdByName(fromJwt.username());
        User user = baseMapper.selectById(id);
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        // 2. 使用刚才写的copyNonNullProperties，只拷贝dto非null字段，避免null覆盖旧值
        BeanUtils.copyProperties(profileDto, user);

        // 3. 更新数据库
        baseMapper.updateById(user);

        // 4. DO转VO返回
        ProfileVo vo = new ProfileVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }


    @Override
    public Boolean password(PasswordDTO passwordDTO) {
        Long id = findIdByName(fromJwt.username());
        User user = baseMapper.selectById(id);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        // 修正matches参数顺序：(前端输入明文旧密码，数据库加密密码)
        boolean bool = passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword());
        if(bool){
            return lambdaUpdate()
                    .eq(User::getId,id)
                    .set(User::getPassword,passwordEncoder.encode(passwordDTO.getNewPassword()))
                    .update();
        }
        throw new BusinessException("原密码输入错误");
    }


    @Autowired
    private IOrderService orderService;

    @Override
    public IPage<CustomerVo> pageAll(CustomerDto dto) {
        Long sellerId = dto.getSellerId();
        List<Long> userIdList = orderService.selectUserIdsBySeller(sellerId);
        long pageNum = dto.getCurrent() == null ? 1L : dto.getCurrent();
        long pageSize = dto.getSize() == null ? 10L : dto.getSize();

        //1.数据库分页，带username phone gender status time等查询条件
        Page<User> userPage = new Page<>(pageNum, pageSize);
        Page<User> dbPage = baseMapper.selectPageAll(userPage, dto,userIdList);
        List<User> userList = dbPage.getRecords();
        if(userList ==  null || userList.isEmpty()){
            Page<CustomerVo> voPage = new Page<>(pageNum,pageSize);
            voPage.setTotal(dbPage.getTotal());
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        //2.拿到当前页所有userId集合，批量一次查询订单统计
        List<Long> userIdList01 = userList.stream().map(User::getId).collect(Collectors.toList());
        //一次性查：每个用户orderCount、totalAmount、lastOrderTime 返回 Map<userId, OrderStat>
        Map<Long, OrderStat> statMap = orderService.batchStat(sellerId, userIdList01);

        //3.组装VO，无循环查询数据库
        List<CustomerVo> voList = new ArrayList<>();
        for(User user : userList){
            CustomerVo vo = new CustomerVo();
            BeanUtils.copyProperties(user,vo);
            vo.setRegisterTime(user.getRegisterTime().toString());
            OrderStat stat = statMap.get(user.getId());
            if(stat != null){
                vo.setOrderCount(stat.getOrderCount());
                vo.setTotalAmount(stat.getTotalAmount());
                vo.setLastOrderTime(stat.getLastOrderTime());
            }else{
                //没有订单的用户给默认值
                vo.setOrderCount(0);
                vo.setTotalAmount(BigDecimal.ZERO);
                vo.setLastOrderTime(null);
            }
            voList.add(vo);
        }

        //组装返回分页对象
        Page<CustomerVo> voPage = new Page<>(pageNum,pageSize);
        voPage.setTotal(dbPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

}
