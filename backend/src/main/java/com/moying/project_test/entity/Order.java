package com.moying.project_test.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单表（单笔订单对应一个商品）
 * </p>
 *
 * @author moying
 * @since 2026-09-05
 */
@Getter
@Setter
@ToString
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（如 ORD20240115001）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 买家用户ID
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 卖家用户ID
     */
    @TableField("seller_id")
    private Long sellerId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 商品名称（下单时快照，冗余存储）
     */
    @TableField("product_name")
    private String productName;

    /**
     * 商品图片（下单时快照，冗余存储）
     */
    @TableField("product_image")
    private String productImage;

    /**
     * 成交单价（下单时快照）
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 订单总金额（= price × quantity）
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 商品总数量（= quantity）
     */
    @TableField("total_quantity")
    private Integer totalQuantity;

    /**
     * 订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
     */
    @TableField("status")
    private Byte status;

    /**
     * 订单备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 收货人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 创建时间（MyBatis自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 支付时间（Service设置）
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 发货时间（Service设置）
     */
    @TableField("ship_time")
    private LocalDateTime shipTime;

    /**
     * 完成时间（Service设置）
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /**
     * 更新时间（MyBatis自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删 1-已删
     */
    @TableField("deleted")
    private Byte deleted;
}
