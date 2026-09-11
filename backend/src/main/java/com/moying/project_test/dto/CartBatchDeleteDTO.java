package com.moying.project_test.dto;

import lombok.Data;

import java.util.List;

/**
 * @author 墨莹
 * @date 2026/8/28 19:09
 */

@Data
public class CartBatchDeleteDTO {
    private List<Long> ids;

}
