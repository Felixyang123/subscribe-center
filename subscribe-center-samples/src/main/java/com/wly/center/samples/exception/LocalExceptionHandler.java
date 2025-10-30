package com.wly.center.samples.exception;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class LocalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled Exception occurred: ", e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("Unhandled RuntimeException occurred: ", e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("Unhandled BusinessException occurred: ", e);
        return Result.fail(e);
    }
}
