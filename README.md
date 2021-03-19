## 项目说明
该分支为非大数据版，可以方便的部署在资源贫乏的服务器例如阿里云学生机上。

## 使用方法

1. 导入sql文件

2. 在项目src/main/resources目录下新建一个application-database.properties文件，然后将数据库配置写到里面
```properties

    spring.datasource.url=
    spring.datasource.username=
    spring.datasource.password=
    spring.datasource.driver-class-name=
    
    spring.datasource.hikari.minimum-idle=3
    spring.datasource.hikari.maximum-pool-size=10
    spring.datasource.hikari.max-lifetime=1800000
    spring.datasource.hikari.connection-test-query=SELECT 1

```   
3. 在项目src/main/resources目录下新建一个application-mail.properties文件，然后将邮件配置写到里面

```properties

spring.mail.host=
spring.mail.username=
spring.mail.password=
spring.mail.properties.from=
spring.mail.properties.mail.smtp.socketFactory.port=465
spring.mail.properties.mail.smtp.ssl.enable=true

```

*注意：* 目前并未做到完全的配置代码分离，仍需要用户手动进代码改的地方有： 
1. `emailTemplate.html` 中的指向域名
2. `index.html` 中底部的域名备案信息