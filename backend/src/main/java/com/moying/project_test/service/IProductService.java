package com.moying.project_test.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moying.project_test.dto.ProductDTO;
import com.moying.project_test.dto.ProductPageQueryDTO;
import com.moying.project_test.entity.Product;
import com.baomidou.mybatisplus.spring.service.IService;
import com.moying.project_test.vo.ProductVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface IProductService extends IService<Product> {

    IPage<ProductVo> findAll(ProductPageQueryDTO dto);

    Boolean addGood(ProductDTO dto, MultipartFile file);

    Boolean updateGoods(ProductDTO dto, MultipartFile file);

    boolean updateStatus(Long id, Byte status);

    List<ProductVo> findTop(int limit);

    List<ProductVo> findHotTop(Integer limit);

    boolean decrease(Long productId, Integer quantity);

    Long findByProductId(Long productId);

    String findImageByProductId(Long productId);
}
