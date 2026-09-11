package com.moying.project_test.exception;

import com.moying.project_test.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 墨莹
 * @date 2026/8/26 16:07
 */


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //自定义业务异常捕捉
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e){

        log.error("业务异常{}" ,e.getMessage(),e);
        return Result.fail(e.getCode(),e.getMessage());

    }


    //参数校验异常捕捉
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {

        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数非法";
        return Result.fail(400, msg);

    }

    //表单提交异常
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {

        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数非法";
        return Result.fail(400, msg);

    }

    /**
     * 兜底：捕获所有其他未知系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleAllException(Exception e) {
        // 开发阶段可以打印堆栈，生产环境日志输出
        e.printStackTrace();
        return Result.fail(500, "系统内部异常，请联系管理员");
    }

}
