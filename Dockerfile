FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

FROM openjdk:26-ea-17-jdk-slim

# Этот образ уже содержит базовые шрифты (Debian-based)
# Если нужны доп шрифты и apt-get работает:
# RUN apt-get update && apt-get install -y fontconfig fonts-dejavu-core

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]