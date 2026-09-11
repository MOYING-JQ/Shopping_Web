package com.moying.project_test.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 墨莹
 * @date 2026/8/27 20:13
 */

@Data
public class ProductPageQueryDTO {

    private Long current;
    private Long size;

    //作用
    private Byte role;
    //子类
    private Long id;
    //user
    private Long userId;
    //商品名称模糊查询
    private String name;
    //分类id
    private Long categoryId;
    //最低价格
    private BigDecimal minPrice;
    //最高价格
    private BigDecimal maxPrice;
    //商品状态
    private Integer status;
    //排序字段 price / create_time
    private String sortField;
    //排序方式 asc / desc
    private String sortOrder;
}
