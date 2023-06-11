package cn.yionr.share.service.exception;

import cn.yionr.share.entity.WebResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;



@ControllerAdvice
public class GlobalExceptionHandler {
    //处理自定义的异常
    @ExceptionHandler(SystemException.class)
    @ResponseBody
    public Object customHandler(SystemException e){
        e.printStackTrace();
        return WebResult.builder().status(e.getCode()).msg(e.getMessage()).build();
    }
    //其他未处理的异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exceptionHandler(Exception e){
        e.printStackTrace();
        return WebResult.builder().status("404").msg("系统错误");
    }
}