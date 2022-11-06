FROM openjdk:8-jre
MAINTAINER yionr <yionr99@gmail.com>
WORKDIR /project
ADD target/share-1.0.0.jar .
ENTRYPOINT ["java","-jar","share-1.0.0.jar"]