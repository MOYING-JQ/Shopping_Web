package com.moying.project_test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moying.project_test.dto.CartBatchDeleteDTO;
import com.moying.project_test.entity.Cart;
import com.moying.project_test.entity.Product;
import com.moying.project_test.mapper.CartMapper;
import com.moying.project_test.service.ICartService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.moying.project_test.service.IProductService;
import com.moying.project_test.service.IUserService;
import com.moying.project_test.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {


    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;



    @Override
    public List<CartVo> getAll() {
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名
        assert authentication != null;
        String username = authentication.getName();
        Long id = userService.findIdByName(username);

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("customer_id", id);

        List<Cart> carts = this.list(queryWrapper);
        List<CartVo> cartVos = new ArrayList<>();
        for(Cart cart : carts){
            CartVo cartVo = new CartVo();
            cartVo.setId(cart.getId());
            cartVo.setProductId(cart.getProductId());
            Product product = productService.getById(cart.getProductId());
            cartVo.setQuantity(cart.getQuantity());
            cartVo.setPrice(product.getPrice());
            cartVo.setProductImage(product.getImage());
            cartVo.setProductName(product.getName());
            cartVo.setStock(product.getStock());
            cartVo.setSelected(cart.getSelected());

            cartVos.add(cartVo);
        }
        return cartVos;
    }

    @Override
    public Integer findAllCount() {
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id
        assert authentication != null;
        String username = authentication.getName();
        Long id = userService.findIdByName(username);

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("customer_id", id);

        return Math.toIntExact(baseMapper.selectCount(queryWrapper));
    }

    @Override
    public Boolean addCart(CartVo cartVo) {

        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id
        assert authentication != null;
        String username = authentication.getName();
        Long id = userService.findIdByName(username);

        boolean exists = lambdaQuery()
                .eq(Cart::getCustomerId,id)
                .eq(Cart::getProductId, cartVo.getProductId())
                .exists();

        if (exists) {
            // 已存在：更新数量，在原有基础上累加
            lambdaUpdate()
                    .eq(Cart::getCustomerId,id)
                    .eq(Cart::getProductId, cartVo.getProductId())
                    .setSql("quantity = quantity + {0}" , cartVo.getQuantity())
                    .update();
        } else {
            // 不存在：新增购物车记录
            Cart cart = new Cart();
            cart.setCustomerId(id);
            cart.setProductId(cartVo.getProductId());
            cart.setQuantity(cartVo.getQuantity());
            ;
            cart.setSelected((byte) 1); // 默认勾选
            save(cart);
        }
        return true;
    }

    @Override
    public Boolean updateCount(Long id, Integer quantity) {


        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id
        assert authentication != null;
        String username = authentication.getName();
        Long userId = userService.findIdByName(username);


        boolean bool = lambdaUpdate()
                .eq(Cart::getCustomerId,userId)
                .eq(Cart::getId,id)
                .set(Cart::getQuantity, quantity)
                .update();
        return bool;
    }

    @Override
    public Boolean updateStatic(Long id, Byte selected) {

        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id
        assert authentication != null;
        String username = authentication.getName();
        Long userId = userService.findIdByName(username);

        return lambdaUpdate()
                .eq(Cart::getCustomerId,userId)
                .eq(Cart::getId,id)
                .set(Cart::getSelected,selected)
                .update();
    }

    @Override
    public Boolean delete(Long id) {

        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id

        assert authentication != null;
        String username = authentication.getName();
        Long userId = userService.findIdByName(username);

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("customer_id", userId);
        queryWrapper.eq("id", id);

        return baseMapper.delete(queryWrapper)>0;
    }

    @Override
    public Boolean deleteBatch(CartBatchDeleteDTO cartBatchDeleteDTO) {

        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id
        assert authentication != null;
        String username = authentication.getName();
        Long userId = userService.findIdByName(username);
        List<Long> ids = cartBatchDeleteDTO.getIds();

        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        queryWrapper.eq("customer_id", userId);
        return baseMapper.delete(queryWrapper)>0;
    }

    @Override
    public Boolean updateAllStatic(Byte selected) {
        // 获取Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取用户名id
        assert authentication != null;
        String username = authentication.getName();
        Long userId = userService.findIdByName(username);

        return lambdaUpdate()
                .eq(Cart::getCustomerId,userId)
                .set(Cart::getSelected, selected)
                .update();
    }


}
