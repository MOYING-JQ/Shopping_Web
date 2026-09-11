package com.moying.project_test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moying.project_test.entity.Category;
import com.moying.project_test.entity.Product;
import com.moying.project_test.exception.BusinessException;
import com.moying.project_test.mapper.CategoryMapper;
import com.moying.project_test.service.ICategoryService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.moying.project_test.vo.CategoryChildVo;
import com.moying.project_test.vo.CategoryVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 商品分类表 服务实现类
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    @Override
    public List<CategoryVo> allList() {

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",0);
        List<Category> categories = this.list(queryWrapper);
        List<CategoryVo> categoryVoList = new ArrayList<>();
        for(Category category : categories){
            CategoryVo categoryVo = new CategoryVo();
            categoryVo.setId(category.getId());
            categoryVo.setName(category.getName());
            categoryVo.setParentId(category.getParentId());
            categoryVo.setStatus(category.getStatus());
            categoryVo.setSort(category.getSort());
            categoryVoList.add(categoryVo);
        }
        return categoryVoList;
    }

    @Override
    public List<CategoryChildVo> findAllByChild() {
        List<CategoryChildVo> categoryChildVos = new ArrayList<>();
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",0);
        //父类
        List<Category> categories = this.list(queryWrapper);
        for(Category category : categories){
            CategoryChildVo categoryChildVo = new CategoryChildVo();
            categoryChildVo.setId(category.getId());
            categoryChildVo.setName(category.getName());
            categoryChildVo.setParentId(category.getParentId());
            categoryChildVo.setSort(category.getSort());
            categoryChildVo.setStatus(category.getStatus());

            //子类
            QueryWrapper<Category> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("parent_id",category.getId());
            List<Category> categories1 = this.list(queryWrapper1);

            List<CategoryChildVo> categoryChildVos1 = new ArrayList<>();
            for(Category category1 : categories1){

                CategoryChildVo categoryChildVo1 = new CategoryChildVo();
                categoryChildVo1.setId(category1.getId());
                categoryChildVo1.setName(category1.getName());
                categoryChildVo1.setParentId(category1.getParentId());
                categoryChildVo1.setSort(category1.getSort());
                categoryChildVo1.setStatus(category1.getStatus());
                List<CategoryChildVo> categoryChildVos2 = new ArrayList<>();
                categoryChildVo1.setChildren(categoryChildVos2);
                categoryChildVos1.add(categoryChildVo1);
            }
            categoryChildVo.setChildren(categoryChildVos1);
            categoryChildVos.add(categoryChildVo);
        }
        return categoryChildVos;
    }

    @Override
    public boolean findByParentCategory(Long parentId) {
        boolean exists = lambdaQuery()
                .eq(Category::getParentId,parentId)
                .exists();
        return exists;
    }

    @Override
    public String findNameById(Long categoryId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",categoryId);
        Category category = baseMapper.selectOne(queryWrapper);
        if(category == null){
            return null;
        }
        return category.getName();
    }
}
