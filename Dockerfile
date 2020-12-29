FROM openjdk:8-jre
MAINTAINER yionr <yionr99@gmail.com>
VOLUME /tmp
ADD share-0.6.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]