FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY src ./src

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

RUN mkdir -p /home/ubuntu/logs

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-Dspring.profiles.active=prod", \
    "-jar", \
    "app.jar"]