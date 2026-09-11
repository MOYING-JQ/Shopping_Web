package com.moying.project_test.service;

import com.moying.project_test.dto.CartBatchDeleteDTO;
import com.moying.project_test.entity.Cart;
import com.baomidou.mybatisplus.spring.service.IService;
import com.moying.project_test.vo.CartVo;

import java.util.List;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface ICartService extends IService<Cart> {

    List<CartVo> getAll();

    Integer findAllCount();

    Boolean addCart(CartVo cartVo);

    Boolean updateCount(Long id, Integer quantity);

    Boolean updateStatic(Long id, Byte selected);

    Boolean delete(Long id);

    Boolean deleteBatch(CartBatchDeleteDTO cartBatchDeleteDTO);

    Boolean updateAllStatic(Byte selected);
}
