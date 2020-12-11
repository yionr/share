package cn.yionr.share;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@MapperScan("cn.yionr.share.mapper")
@ServletComponentScan("cn.yionr.share.filter")
public class ShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareApplication.class , args);
    }

}
