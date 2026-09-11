package com.moying.project_test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.common.Result;
import com.moying.project_test.dto.ProductDTO;
import com.moying.project_test.dto.ProductPageQueryDTO;
import com.moying.project_test.entity.Product;
import com.moying.project_test.service.IProductService;
import com.moying.project_test.vo.ProductVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author 墨莹
 * @date 2026/8/27 18:53
 */

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping("/page")
    public Result<IPage<ProductVo>> page(@ModelAttribute ProductPageQueryDTO dto) {

        IPage<ProductVo> productVoPage = productService.findAll(dto);
        return Result.success(productVoPage);
    }


    @GetMapping("/{id}")
    public Result<ProductVo> getProductById(@PathVariable Long id) {

        Product product = productService.getById(id);

        ProductVo productVo = new ProductVo();
        BeanUtils.copyProperties(product, productVo);
        return Result.success(productVo);
    }

    @PostMapping
    public Result<Boolean> add(@ModelAttribute ProductDTO dto,
                               @RequestParam(value = "file", required = false) MultipartFile file) {

        Boolean bool = productService.addGood(dto, file);

        return Result.success(bool);

    }

    @PutMapping
    public Result<Boolean> update(@ModelAttribute ProductDTO dto,
                                  @RequestParam(value = "file", required = false) MultipartFile file) {
        Boolean bool = productService.updateGoods(dto, file);

        return Result.success(bool);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {

        boolean ok = productService.removeById(id);
        return Result.success(ok);
    }

    @PutMapping("/status/{id}/{status}")
    public Result<Boolean> updateStatus(@PathVariable Long id, @PathVariable Byte status) {

        boolean bool = productService.updateStatus(id,status);
        return Result.success(bool);
    }

    @GetMapping("/top-sales")
    public Result<List<ProductVo>> topSales(@RequestParam(defaultValue = "10") Integer limit) {
        List<ProductVo> productVo = productService.findTop(limit);
        return Result.success(productVo);
    }

    @GetMapping("/mall-hot")
    public Result<List<ProductVo>> mallHot(@RequestParam(defaultValue = "10") Integer limit) {
        List<ProductVo> productVo = productService.findHotTop(limit);
        return Result.success(productVo);
    }
}
