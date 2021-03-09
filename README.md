
[TOC]

## 项目说明
该项目是我的毕设项目《基于HDFS的云文件分享系统》，主要实现了文件的临时云存储以及云分享。发送方往该项目传输一个临时文件，设置密码和允许的下载次数，就会得到一个取件码。接收方通过该项目输入该取件码即可获取到那个临时文件。

## 使用方法

1. 导入sql

2. 命令行进入本项目share/docker 运行`docker-compose up -d` 该命令将会创建一个hadoop集群+hbase 

3. 修改本机hosts文件，添加三条记录
```hosts

127.0.0.1	docker-hbase
127.0.0.1	docker-datanode
127.0.0.1	docker-namenode

```

4. 在项目src/main/resources目录下新建一个application-database.properties文件，然后将数据库配置写到里面
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
5. 在项目src/main/resources目录下新建一个application-mail.properties文件，然后将邮件配置写到里面
```properties
spring.mail.host=
spring.mail.username=
spring.mail.password=
spring.mail.properties.from=
spring.mail.properties.mail.smtp.socketFactory.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```
