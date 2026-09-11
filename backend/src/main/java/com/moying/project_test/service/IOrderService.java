package com.moying.project_test.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moying.project_test.dto.OrderCreateDTO;
import com.moying.project_test.dto.OrderQueryDTO;
import com.moying.project_test.entity.Order;
import com.baomidou.mybatisplus.spring.service.IService;
import com.moying.project_test.entity.OrderStat;
import com.moying.project_test.vo.OrderVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表（单笔订单对应一个商品） 服务类
 * </p>
 *
 * @author moying
 * @since 2026-09-05
 */
public interface IOrderService extends IService<Order> {

    IPage<OrderVO> fangAll(OrderQueryDTO orderQueryDTO);

    IPage<OrderVO> fangAllBySell(OrderQueryDTO orderQueryDTO);

    OrderVO findById(Long id);


    Long addOrder(OrderCreateDTO dto);

    Boolean updateStatus(Long id, Byte status);

    Map<Long, OrderStat> batchStat(Long sellerId, List<Long> userIdList);

    List<Long> selectUserIdsBySeller(Long sellerId);
}
