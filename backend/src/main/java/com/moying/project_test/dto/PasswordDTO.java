package com.moying.project_test.dto;

import lombok.Data;

/**
 * @author 墨莹
 * @date 2026/9/6 13:51
 */

@Data
public class PasswordDTO {
    private String oldPassword;
    private String newPassword;
}
