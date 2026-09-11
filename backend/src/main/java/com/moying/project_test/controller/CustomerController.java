package com.moying.project_test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moying.project_test.common.Result;
import com.moying.project_test.dto.CustomerDto;
import com.moying.project_test.service.IUserService;
import com.moying.project_test.vo.CustomerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 墨莹
 * @date 2026/9/6 14:16
 */

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private IUserService userService;

    @GetMapping("/page")
    public Result<IPage<CustomerVo>> page(@ModelAttribute CustomerDto  customerDto) {

        IPage<CustomerVo> customerVoIPage = userService.pageAll(customerDto);
        return Result.success(customerVoIPage);
    }
}
