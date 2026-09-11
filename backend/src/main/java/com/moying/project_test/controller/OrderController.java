package com.moying.project_test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.common.Result;
import com.moying.project_test.dto.OrderCreateDTO;
import com.moying.project_test.dto.OrderQueryDTO;
import com.moying.project_test.entity.Order;
import com.moying.project_test.service.IOrderService;
import com.moying.project_test.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 订单表（单笔订单对应一个商品） 前端控制器
 * </p>
 *
 * @author moying
 * @since 2026-09-05
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @GetMapping("/page")
    public Result<IPage<OrderVO>> page(@ModelAttribute OrderQueryDTO orderQueryDTO) {

        IPage<OrderVO> page = orderService.fangAll(orderQueryDTO);
        return Result.success(page);

    }

    @GetMapping("/seller/page")
    public Result<IPage<OrderVO>> pageBySeller(@ModelAttribute OrderQueryDTO orderQueryDTO) {
        IPage<OrderVO> page = orderService.fangAllBySell(orderQueryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> pageById(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.findById(id);
        return Result.success(orderVO);
    }

    @PostMapping
    public Result<Long> add(@RequestBody OrderCreateDTO dto) {
        Long id = orderService.addOrder(dto);
        return Result.success(id);
    }

    @PutMapping("/status/{id}/{status}")
    public Result<Boolean> update(@PathVariable("id") Long id, @PathVariable("status") Byte status) {

        System.out.println("-------------------------------------ID"+status+id);
        Boolean bool = orderService.updateStatus(id,status);
        return bool?Result.success(bool):Result.fail("修改订单失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        Boolean bool = orderService.removeById(id);
        return Result.success(bool);
    }
}
