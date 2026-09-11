package com.moying.project_test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author 墨莹
 * @date 2026/8/26 12:20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3 ,max = 20,message = "3-20位")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6 ,max = 20,message = "6-20位")
    private String password;
}
