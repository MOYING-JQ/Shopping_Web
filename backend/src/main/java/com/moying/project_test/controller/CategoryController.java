package com.moying.project_test.controller;

import com.moying.project_test.common.Result;
import com.moying.project_test.dto.RefreshDto;
import com.moying.project_test.service.ICategoryService;
import com.moying.project_test.service.impl.CategoryServiceImpl;
import com.moying.project_test.vo.CategoryChildVo;
import com.moying.project_test.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨莹
 * @date 2026/8/28 10:05
 */



@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private ICategoryService  categoryService;

    @GetMapping("/list")
    public Result<List<CategoryVo>> category() {
        List<CategoryVo> categoryVoList = categoryService.allList();
        return Result.success(categoryVoList);
    }

    @GetMapping("/tree")
    public Result<List<CategoryChildVo>>  categoryTree() {
        List<CategoryChildVo> categoryChildVos = categoryService.findAllByChild();
        return   Result.success(categoryChildVos);
    }
}
