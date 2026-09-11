package com.moying.project_test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moying.project_test.dto.CustomerDto;
import com.moying.project_test.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author moying
 * @since 2026-08-26
 */
public interface UserMapper extends BaseMapper<User> {

    Page<User> selectPageAll(Page<User> userPage, @Param("dto") CustomerDto dto, @Param("userIdList") List<Long> userIdList);
}
