package com.moying.project_test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.dto.ProductPageQueryDTO;
import com.moying.project_test.entity.Product;
import com.moying.project_test.vo.ProductVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface ProductMapper extends BaseMapper<Product> {

    IPage<ProductVo> selectProductPage(Page<ProductVo> page, @Param("dto") ProductPageQueryDTO dto);

}
