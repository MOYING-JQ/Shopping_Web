package com.moying.project_test.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/9/6 14:22
 */

@Data
public class CustomerVo {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private Byte gender;
    private Byte role;
    private String avatar;
    private String nickname;
    private Integer level;
    private Byte status;
    private String registerTime;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private String lastOrderTime;
}
