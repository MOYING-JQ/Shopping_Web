package com.moying.project_test.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/9/5 16:33
 */
@Data
public class OrderCreateDTO {

    private Long customerId;
    private Long sellerId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String remark;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
}
