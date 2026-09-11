package com.moying.project_test.controller;

import com.moying.project_test.common.Result;
import com.moying.project_test.service.DashboardService;
import com.moying.project_test.vo.DashboardVo;
import com.moying.project_test.vo.SalesTrendVo;
import com.moying.project_test.vo.StatusVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据看板控制器
 *
 * @author 墨莹
 * @date 2026/9/9 11:26
 */

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardVo> overview() {
        DashboardVo dashboardVo = dashboardService.getTotal();
        return Result.success(dashboardVo);
    }

    @GetMapping("/sales-trend")
    public Result<List<SalesTrendVo>> salesTrend() {
        List<SalesTrendVo> salesTrendVo = dashboardService.getTrend();
        return Result.success(salesTrendVo);
    }

    @GetMapping("/order-status")
    public Result<List<StatusVo>> orderStatus() {
        List<StatusVo> statusVos = dashboardService.fandStatus();
        return Result.success(statusVos);
    }
}
