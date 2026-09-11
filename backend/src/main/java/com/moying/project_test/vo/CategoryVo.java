package com.moying.project_test.vo;

import lombok.Data;

/**
 * @author 墨莹
 * @date 2026/8/28 10:19
 */

@Data
public class CategoryVo {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private Byte status;
}
