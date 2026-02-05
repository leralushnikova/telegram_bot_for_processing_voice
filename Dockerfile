FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

FROM ubuntu:22.04

# Установка пакетов для Ubuntu
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        fontconfig \
        fonts-dejavu-core \
        libfreetype6 \
    && rm -rf /var/lib/apt/lists/*

# Копирование кастомных шрифтов
COPY fonts/*.ttf /usr/share/fonts/truetype/custom/

# Обновление кэша шрифтов
RUN fc-cache -f

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]