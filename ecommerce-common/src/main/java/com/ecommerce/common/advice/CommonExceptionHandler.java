package com.ecommerce.common.advice;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// This annotation will automatically check all controllers by default.
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(EException.class) // Processing common type of exceptions, which can reduce code repetition and complexity
    public ResponseEntity<ExceptionResult> handleException(EException ee){
        ExceptionEnum em = ee.getExceptionEnum();
        return ResponseEntity.status(em.getCode()).body(new ExceptionResult(em));
    }
}