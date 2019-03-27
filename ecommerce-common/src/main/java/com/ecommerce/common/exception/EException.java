package com.ecommerce.common.exception;

import com.ecommerce.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EException extends RuntimeException {
    private ExceptionEnum exceptionEnum;
}
