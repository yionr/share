FROM openjdk:8-jre
MAINTAINER yionr <yionr99@gmail.com>
WORKDIR /project
ADD target/share-0.6.1.jar .
ENTRYPOINT ["java","-jar","share-0.6.1.jar"]