FROM maven:3.9-amazoncorretto-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine-jdk

# Устанавливаем шрифты
#RUN apk add --no-cache fontconfig ttf-dejavu

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]