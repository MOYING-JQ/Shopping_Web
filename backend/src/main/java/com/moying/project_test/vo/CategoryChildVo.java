package com.moying.project_test.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 墨莹
 * @date 2026/8/28 11:15
 */
@Data
public class CategoryChildVo {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private Byte status;
    private List<CategoryChildVo> children;
}
