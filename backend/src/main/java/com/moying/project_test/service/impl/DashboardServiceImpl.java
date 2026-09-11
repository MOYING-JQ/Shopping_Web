package com.moying.project_test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.moying.project_test.entity.Order;
import com.moying.project_test.entity.Product;
import com.moying.project_test.entity.User;
import com.moying.project_test.exception.BusinessException;
import com.moying.project_test.mapper.OrderMapper;
import com.moying.project_test.mapper.ProductMapper;
import com.moying.project_test.mapper.UserMapper;
import com.moying.project_test.service.DashboardService;
import com.moying.project_test.util.FromJwt;
import com.moying.project_test.vo.DashboardVo;
import com.moying.project_test.vo.SalesTrendVo;
import com.moying.project_test.vo.StatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 墨莹
 * @date 2026/9/9 11:32
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FromJwt fromJwt;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 从 JWT 中取出当前登录用户，未登录直接抛业务异常
     */
    private User getCurrentUser() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", fromJwt.username());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("请登录");
        }
        return user;
    }

    @Override
    public DashboardVo getTotal() {
        DashboardVo dashboardVo = new DashboardVo();
        User user = getCurrentUser();
        if (user == null) {
            // 理论上不会走到：getCurrentUser 已抛异常；这里兜底返回全0
            dashboardVo.setTotalSales(BigDecimal.ZERO);
            dashboardVo.setTotalOrders(0L);
            dashboardVo.setTotalCustomers(0L);
            dashboardVo.setTotalProducts(0L);
            return dashboardVo;
        }
        Long id = user.getId();

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 3)
                .eq("seller_id", id)
                .select("COALESCE(SUM(total_amount),0) as totalSum",
                        "COUNT(*) as totalCount",
                        "COUNT(DISTINCT customer_id) as userCount");
        List<Map<String, Object>> list = orderMapper.selectMaps(wrapper);

        // 取出那一行map
        Map<String, Object> resultMap = list.get(0);
        // 拿到totalSum，转BigDecimal
        BigDecimal totalSales = new BigDecimal(resultMap.get("totalSum").toString());
        Long totalOrders = Long.parseLong(resultMap.get("totalCount").toString());
        Long totalCustomers = Long.parseLong(resultMap.get("userCount").toString());
        dashboardVo.setTotalSales(totalSales);
        dashboardVo.setTotalOrders(totalOrders);
        dashboardVo.setTotalCustomers(totalCustomers);

        LambdaQueryWrapper<Product> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(Product::getSellerId, id);
        // 查询商品总数量
        Long productCount = productMapper.selectCount(lambdaQueryWrapper);
        dashboardVo.setTotalProducts(productCount);

        return dashboardVo;
    }

    @Override
    public List<SalesTrendVo> getTrend() {
        Long id = getCurrentUser().getId();

        List<SalesTrendVo> dailyList = orderMapper.getTrend(id);

        // 把数据库结果放进Map，key=日期字符串 yyyy-MM-dd
        Map<String, SalesTrendVo> dataMap = new HashMap<>();
        for (SalesTrendVo vo : dailyList) {
            dataMap.put(vo.getDate(), vo);
        }

        List<SalesTrendVo> salesTrendVos = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 【核心：最近7天】当前日期为最后一天，向前推6天，总共7天
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(formatter);
            SalesTrendVo vo;
            if (dataMap.containsKey(dateStr)) {
                // 当日存在订单数据，直接取出
                vo = dataMap.get(dateStr);
            } else {
                // 当日没有订单，新建VO，数值置0
                vo = new SalesTrendVo();
                vo.setDate(dateStr);
                vo.setAmount(BigDecimal.ZERO);
                vo.setOrderCount(0);
            }
            salesTrendVos.add(vo);
            current = current.plusDays(1);
        }

        log.info("最近7天销售数据：{}", salesTrendVos);
        return salesTrendVos;
    }

    @Override
    public List<StatusVo> fandStatus() {
        Long id = getCurrentUser().getId();
        List<StatusVo> statusVos = orderMapper.fandStatus(id);
        for (StatusVo vo : statusVos) {
            String text = switch (vo.getName()) {
                case "0" -> "待付款";
                case "1" -> "已付款";
                case "2" -> "已发货";
                case "3" -> "已完成";
                case "4" -> "已取消";
                default -> "未知状态";
            };
            vo.setName(text);
        }
        return statusVos;
    }
}
