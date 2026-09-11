package com.moying.project_test.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/9/9 11:28
 */

@Data
public class DashboardVo {

    private BigDecimal totalSales;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
}
