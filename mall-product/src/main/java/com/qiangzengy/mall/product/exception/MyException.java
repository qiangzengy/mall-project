package com.qiangzengy.mall.product.exception;

import com.qiangzengy.common.enums.ExceptionCode;
import com.qiangzengy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.qiangzengy.mall.product.controller")//需要处理异常的包
public class MyException {


    //该注解：需要处理的哪些异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){

        log.error("数据校验出现问题{}，异常类型：{}",e.getMessage(),e.getClass());

        //异常信息
        BindingResult result=e.getBindingResult();
        Map<String,String>errorMap=new HashMap<>();
        //1、获取校验的错误结果
        result.getFieldErrors().forEach((fieldError) -> {
            //获取错误的属性的名字 fieldError.getField()
            //获取到错误提示 fieldError.getDefaultMessage()
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });

        return R.error(ExceptionCode.VAILD_EXCEPTION.getCode(),ExceptionCode.VAILD_EXCEPTION.getMsg()).put("data",errorMap);
    }

    //该注解：需要处理的哪些异常
    @ExceptionHandler(value = Exception.class)
    public R handleVaildException(Exception e){
        log.error("错误",e);
        return R.error(ExceptionCode.UN_KNOW_EXCEPTION.getCode(),ExceptionCode.UN_KNOW_EXCEPTION.getMsg());
    }

}
