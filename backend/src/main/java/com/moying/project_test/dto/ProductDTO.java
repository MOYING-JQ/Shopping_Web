package com.moying.project_test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/9/2 22:15
 */
@Data

public class ProductDTO {

        private Long id;
        /**
         * 商品名称
         */
        private String name;

        /**
         * 分类ID
         */
        private Long categoryId;

        /**
         * 售价
         */
        private BigDecimal price;

        /**
         * 原价
         */
        private BigDecimal originalPrice;

        /**
         * 库存
         */
        private Integer stock;

        /**
         * 商品描述
         */
        private String description;

        /**
         * 0下架 /1上架 默认1
         */
        private Byte status;

        /**
         * 图片url
         */
        private String image;

}
