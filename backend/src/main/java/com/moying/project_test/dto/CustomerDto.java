package com.moying.project_test.dto;

import lombok.Data;

/**
 * @author 墨莹
 * @date 2026/9/6 14:26
 */

@Data
public class CustomerDto {

    private Long sellerId;
    private String username;
    private String phone;
    private Integer gender;
    private Integer level;
    private Byte status;
    private String startTime;
    private String endTime;
    private Integer current;
    private Integer size;
}
