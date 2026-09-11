package com.moying.project_test.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 墨莹
 * @date 2026/8/26 12:27
 */

@Data
public class LoginVo {

    private String accessToken;
    private String refreshToken;

}
