package com.moying.project_test.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/8/28 13:57
 */

@Data
public class CartVo {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Byte selected;
}
