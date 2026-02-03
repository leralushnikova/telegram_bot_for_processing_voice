FROM maven:3.9-amazoncorretto-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

# Используем slim образ вместо alpine - он стабильнее
FROM amazoncorretto:17-slim

# Устанавливаем шрифты (Debian-based, быстрее)
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]