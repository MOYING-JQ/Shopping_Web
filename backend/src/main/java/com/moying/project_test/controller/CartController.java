package com.moying.project_test.controller;

import com.moying.project_test.common.Result;
import com.moying.project_test.dto.CartBatchDeleteDTO;
import com.moying.project_test.service.ICartService;
import com.moying.project_test.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 墨莹
 * @date 2026/8/28 14:01
 */

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/list")
    public Result<List<CartVo>> getList(){

        List<CartVo> cartVos = cartService.getAll();
        return Result.success(cartVos);
    }

    @GetMapping("/count")
    public Result<Integer> getCount(){
        Integer num = cartService.findAllCount();
        return Result.success(num);
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody CartVo cartVo){

        Boolean bool = cartService.addCart(cartVo);
        return bool?Result.success(bool):Result.fail("添加失败");
    }

    @PutMapping("/quantity/{id}/{quantity}")
    public Result<Boolean> addQuantity(@PathVariable("id") Long id, @PathVariable("quantity") Integer quantity){

        Boolean bool = cartService.updateCount(id,quantity);
        return bool?Result.success(bool):Result.fail("增加失败");
    }

    @PutMapping("/select/{id}/{selected}")
    public Result<Boolean> changeSelect(@PathVariable("id") Long id, @PathVariable("selected") Byte selected){

        Boolean bool = cartService.updateStatic(id,selected);
        return bool?Result.success(bool):Result.fail("修改失败");

    }

    @PutMapping("/select-all/{selected}")
    public Result<Boolean> changeSelectAll(@PathVariable("selected") Byte selected){

        Boolean bool = cartService.updateAllStatic(selected);
        return bool?Result.success(bool):Result.fail("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id){

        Boolean bool = cartService.delete(id);
        return bool?Result.success(bool):Result.fail("删除失败");
    }

    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody CartBatchDeleteDTO cartBatchDeleteDTO){
        Boolean bool = cartService.deleteBatch(cartBatchDeleteDTO);
        return bool?Result.success(bool):Result.fail("删除失败");
    }
}
