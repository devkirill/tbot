FROM openjdk:11

EXPOSE 8888

RUN mkdir /app
RUN ls /home/

COPY ./build/libs/*-SNAPSHOT.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/spring-boot-application.jar"]