package cn.yionr.share.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
@Slf4j
public class FileServiceAspect {
    long startTime;
    @Before("execution(* cn.yionr.share.service.FileService.upload(..))")
    public void beforeUpload(){
        startTime = new Date().getTime();
    }

    @After("execution(* cn.yionr.share.service.FileService.upload(..))")
    public void afterUpload(){
        log.info("上传已完成,本次上传总用时: {} ms",new Date().getTime() - startTime);
    }
}
