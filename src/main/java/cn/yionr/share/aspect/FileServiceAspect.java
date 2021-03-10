package cn.yionr.share.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class FileServiceAspect {
//    @AfterThrowing("execution(* cn.yionr.share.service.FileService.*(..))")
//    public void exceptionAdvice(){
//        log.warn();
//    }
}
