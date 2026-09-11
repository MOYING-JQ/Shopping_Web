package com.moying.project_test.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author 墨莹
 * @date 2026/9/9 12:40
 */
@Data
public class SalesTrendVo {

    /**
     * 日期 yyyy-MM-dd
     */
    private String date;
    /**
     * 当日销售总额
     */
    private BigDecimal amount;
    /**
     * 当日订单数量
     */
    private Integer orderCount;

}
