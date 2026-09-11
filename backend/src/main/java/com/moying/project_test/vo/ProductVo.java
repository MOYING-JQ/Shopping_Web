package com.moying.project_test.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author 墨莹
 * @date 2026/8/27 19:01
 */

@Data
public class ProductVo {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private String image;
    private String description;
    private Byte status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
