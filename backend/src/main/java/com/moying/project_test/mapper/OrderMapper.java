package com.moying.project_test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.dto.OrderQueryDTO;
import com.moying.project_test.entity.OrderStat;
import com.moying.project_test.vo.OrderVO;
import com.moying.project_test.vo.SalesTrendVo;
import com.moying.project_test.vo.StatusVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface OrderMapper extends BaseMapper<com.moying.project_test.entity.Order> {

    IPage<OrderVO> selectOrderPage(Page<OrderVO> page, @Param("dto") OrderQueryDTO dto);

    IPage<OrderVO> selectOrderPageBySell(Page<OrderVO> page,@Param("dto") OrderQueryDTO dto);


    List<OrderStat> batchSelectStat(Long sellerId, List<Long> userIdList);

    List<SalesTrendVo> getTrend(@Param("sellerId") Long id);

    List<StatusVo> fandStatus(@Param("sellerId")Long id);
}

