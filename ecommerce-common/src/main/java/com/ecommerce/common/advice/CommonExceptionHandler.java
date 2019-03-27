package com.ecommerce.common.advice;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// 这个注解会默认情况下自动拦截所有的controller
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(EException.class) // 统一处理某一类异常，从而能够减少代码重复率和复杂度
    public ResponseEntity<ExceptionResult> handleException(EException ee){
        ExceptionEnum em = ee.getExceptionEnum();
        return ResponseEntity.status(em.getCode()).body(new ExceptionResult(em));
    }
}