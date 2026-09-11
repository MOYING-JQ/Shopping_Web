package com.moying.project_test.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 墨莹
 * @date 2026/8/27 16:31
 */

@Data
public class InfoVo {

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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime registerTime;
}
