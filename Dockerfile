FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

FROM debian:12-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    libfreetype6 \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]