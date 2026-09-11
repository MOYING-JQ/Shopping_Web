package com.moying.project_test.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/8/28 13:59
 */

@Data
public class CartDto {

    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Integer selected;
}
