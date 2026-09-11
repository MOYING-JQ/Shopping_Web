package com.moying.project_test.service;

import com.moying.project_test.vo.DashboardVo;
import com.moying.project_test.vo.SalesTrendVo;
import com.moying.project_test.vo.StatusVo;

import java.util.List;

/**
 * @author 墨莹
 * @date 2026/9/9 11:32
 */
public interface DashboardService {
    DashboardVo getTotal();

    List<SalesTrendVo> getTrend();

    List<StatusVo> fandStatus();

}
