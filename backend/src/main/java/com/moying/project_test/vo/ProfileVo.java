package com.moying.project_test.vo;

import lombok.Data;

/**
 * @author 墨莹
 * @date 2026/9/6 13:24
 */

@Data
public class ProfileVo {
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
    private String updateTime;
}
