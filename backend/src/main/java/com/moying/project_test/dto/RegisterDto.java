package com.moying.project_test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 墨莹
 * @date 2026/8/26 16:28
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3 ,max = 20,message = "3-20位")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6 ,max = 20,message = "6-20位")
    private String password;
    private String email;
    private String phone;
    private Byte role;
}
