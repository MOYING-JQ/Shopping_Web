package com.moying.project_test.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.dto.OrderCreateDTO;
import com.moying.project_test.dto.OrderQueryDTO;
import com.moying.project_test.entity.Order;
import com.moying.project_test.entity.OrderStat;
import com.moying.project_test.exception.BusinessException;
import com.moying.project_test.mapper.OrderMapper;
import com.moying.project_test.service.IOrderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.moying.project_test.service.IProductService;
import com.moying.project_test.service.IUserService;
import com.moying.project_test.util.FromJwt;
import com.moying.project_test.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 订单表（单笔订单对应一个商品） 服务实现类
 * </p>
 *
 * @author moying
 * @since 2026-09-05
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private static final Set<String> ALLOW_SORT = Set.of("total_amount","create_time" ,"total_quantity");
    private static final Set<String> ALLOW_ORDER = Set.of("asc", "desc");



    @Autowired
    private FromJwt fromJwt;

    @Autowired
    private IProductService productService;



    @Override
    public IPage<OrderVO> fangAll(OrderQueryDTO dto) {
        //排序白名单校验，防止SQL注入
        if(dto.getSortField() == null || !ALLOW_SORT.contains(dto.getSortField())){
            dto.setSortField("create_time");
        }
        if(dto.getSortOrder() == null || !ALLOW_ORDER.contains(dto.getSortOrder())){
            dto.setSortOrder("desc");
        }

        long pageNum = dto.getCurrent() == null ? 1L : dto.getCurrent();
        long pageSize = dto.getSize() == null ? 10L : dto.getSize();

        Page<OrderVO> page = new Page<>(pageNum, pageSize);
        page.setOptimizeCountSql(false);


        return baseMapper.selectOrderPage(page,dto);
    }

    @Override
    public IPage<OrderVO> fangAllBySell(OrderQueryDTO dto) {
        if(dto.getSortField() == null || !ALLOW_SORT.contains(dto.getSortField())){
            dto.setSortField("create_time");
        }
        if(dto.getSortOrder() == null || !ALLOW_ORDER.contains(dto.getSortOrder())){
            dto.setSortOrder("desc");
        }

        long pageNum = dto.getCurrent() == null ? 1L : dto.getCurrent();
        long pageSize = dto.getSize() == null ? 10L : dto.getSize();


        //把CustomerId变成卖家id在数据库里面查到seller——id

        Page<OrderVO> page = new Page<>(pageNum, pageSize);
        page.setOptimizeCountSql(false);


        return baseMapper.selectOrderPageBySell(page,dto);
    }

    @Override
    public OrderVO findById(Long id) {

        Order order = baseMapper.selectById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        return orderVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addOrder(OrderCreateDTO dto) {
        // 1.先扣库存
        boolean bool = productService.decrease(dto.getProductId(), dto.getQuantity());
        if (!bool){
            throw new BusinessException("库存不足，扣减失败");
        }
        // 2.再创建订单
        Long sellerId = productService.findByProductId(dto.getProductId());
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setProductId(dto.getProductId());
        order.setQuantity(dto.getQuantity());
        Byte b = 0;
        order.setDeleted(b);
        order.setProductImage(productService.findImageByProductId(dto.getProductId()));
        order.setStatus(b);
        order.setSellerId(sellerId);
        order.setProductName(dto.getProductName());
        order.setPrice(dto.getPrice());
        // price是BigDecimal，quantity是Integer
        BigDecimal all = dto.getPrice().multiply(new BigDecimal(dto.getQuantity()));
        order.setTotalAmount(all);
        order.setTotalQuantity(dto.getQuantity());
        order.setRemark(dto.getRemark());
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        String timeStamp = String.valueOf(System.currentTimeMillis());
        order.setOrderNo(timeStamp);
        int result = baseMapper.insert(order);

        if(result == 1){
            return order.getId();
        }
        throw new BusinessException("添加订单失败");
    }

    @Override
    public Boolean updateStatus(Long id, Byte status) {
        if (status == 1) {
            return lambdaUpdate()
                    .eq(Order::getId,id)
                    .set(Order::getStatus,status)
                    .set(Order::getPayTime, LocalDateTime.now())
                    .update();
        }
        else if (status == 2) {
            return lambdaUpdate()
                    .eq(Order::getId,id)
                    .set(Order::getStatus,status)
                    .set(Order::getShipTime, LocalDateTime.now())
                    .update();
        }
        else if (status == 3) {
            return lambdaUpdate()
                    .eq(Order::getId,id)
                    .set(Order::getStatus,status)
                    .set(Order::getFinishTime, LocalDateTime.now())
                    .update();
        }
        else{
            return lambdaUpdate()
                    .eq(Order::getId,id)
                    .set(Order::getStatus,status)
                    .update();
        }

    }

    @Override
    public Map<Long, OrderStat> batchStat(Long sellerId, List<Long> userIdList) {

        if(userIdList == null || userIdList.isEmpty()){
            return new HashMap<>();
        }
        //批量查询，拿到List<OrderStat>
        List<OrderStat> statList = baseMapper.batchSelectStat(sellerId, userIdList);

        //list转map key=userId
        Map<Long, OrderStat> statMap = new HashMap<>();
        for (OrderStat stat : statList) {
            statMap.put(stat.getUserId(), stat);
        }
        return statMap;
    }

    @Override
    public List<Long> selectUserIdsBySeller(Long sellerId) {
        List<Order> orders = lambdaQuery().eq(Order::getSellerId, sellerId).list();
        List<Long> userIdList = new ArrayList<>();
        for (Order order : orders) {
            userIdList.add(order.getCustomerId());
        }
        return userIdList;
    }
}
