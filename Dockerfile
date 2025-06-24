FROM openjdk:17

WORKDIR /app

RUN mkdir -p logs

COPY build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=real", "-jar", "app.jar"]