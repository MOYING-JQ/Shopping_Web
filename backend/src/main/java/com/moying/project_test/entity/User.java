package com.moying.project_test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
@Getter
@Setter
@ToString
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    @TableField("password")
    private String password;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 性别: 0-未知 1-男 2-女
     */
    @TableField("gender")
    private Byte gender;

    /**
     * 角色: 0-买家 1-卖家
     */
    @TableField("role")
    private Byte role;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 会员等级: 1-普通 2-银卡 3-金卡
     */
    @TableField("level")
    private Integer level;

    /**
     * 状态: 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 注册时间
     */
    @TableField("register_time")
    private LocalDateTime registerTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删 1-已删
     */
    @TableField("deleted")
    private Byte deleted;
}
