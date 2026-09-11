package com.moying.project_test.exception;

import com.moying.project_test.common.Result;
import lombok.Data;

/**
 * @author 墨莹
 * @date 2026/8/26 15:56
 */

@Data
public class BusinessException extends RuntimeException {

    private Integer code;

    public  BusinessException(String message){
        super(message);
        this.code = 500;
    }

    public  BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
