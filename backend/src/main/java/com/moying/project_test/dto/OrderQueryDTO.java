package com.moying.project_test.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author 墨莹
 * @date 2026/9/5 11:38
 */

@Data
public class OrderQueryDTO {
    /**
     * 订单编号(模糊)
     */
    private String orderNo;

    /**
     * 用户ID(买家查看自己的订单)
     */
    private Long customerId;

    /**
     * 用户名(模糊，卖家查看所有)
     */
    private String customerName;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    private BigDecimal maxAmount;

    /**
     * 起始时间
     */
    private LocalDateTime startTime;

    /**
     * 截止时间
     */
    private LocalDateTime endTime;

    /**
     * 排序字段：total_amount / create_time / total_quantity
     */
    private String sortField;

    /**
     * 排序方式 asc / desc
     */
    private String sortOrder;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    private Long sellerId;
}
