package com.southwind.aispringboot.common;

import com.southwind.aispringboot.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobarExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handlerValidException(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        Result<String> result = Result.error(ResultCode.PARAM_ERROR.getCode(), message, null);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<String>> handlerHttpMessageNotReadableException(HttpMessageNotReadableException e){
        Result<String> result = Result.error(ResultCode.PARAM_ERROR.getCode(), "请求参数格式错误，请检查JSON格式是否正确", null);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Result<String>> handlerParamException(Exception e){
        Result<String> result = Result.error(ResultCode.PARAM_ERROR.getCode(), ResultCode.PARAM_ERROR.getMsg(), null);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e){
        Result<?> result;
        if (e.getData() != null){
            result = Result.error(e.getCode(), e.getMessage(), e.getData());
        } else {
            result = Result.error(e.getCode(), e.getMessage(), null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e){
        e.printStackTrace();
        Result<String> result = Result.error(ResultCode.SYSTEM_ERROR.getCode(), e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
