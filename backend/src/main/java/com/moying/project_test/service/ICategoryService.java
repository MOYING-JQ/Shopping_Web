package com.moying.project_test.service;

import com.moying.project_test.entity.Category;
import com.baomidou.mybatisplus.spring.service.IService;
import com.moying.project_test.vo.CategoryChildVo;
import com.moying.project_test.vo.CategoryVo;

import java.util.List;

/**
 * <p>
 * 商品分类表 服务类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface ICategoryService extends IService<Category> {

    List<CategoryVo> allList();

    List<CategoryChildVo> findAllByChild();

    boolean findByParentCategory(Long parentId);

    String findNameById(Long categoryId);
}
