package com.moying.project_test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
@Getter
@Setter
@ToString
@TableName("t_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    @TableField("seller_id")
    private Long sellerId;


    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 销售价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价（划线价）
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 商品主图URL
     */
    @TableField("image")
    private String image;

    /**
     * 商品描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态: 0-下架 1-上架
     */
    @TableField("status")
    private Byte status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删 1-已删
     */
    @TableField("deleted")
    private Byte deleted;
}
