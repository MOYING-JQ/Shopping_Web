package com.moying.project_test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 墨莹
 * @date 2026/8/27 17:22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshDto {
    public String refreshToken;
}
