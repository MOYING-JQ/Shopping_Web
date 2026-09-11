package com.moying.project_test.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/9/6 17:08
 */

@Data
public class OrderStat {
    private Long userId;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private String lastOrderTime;
}
