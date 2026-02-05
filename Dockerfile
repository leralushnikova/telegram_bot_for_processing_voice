FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine

RUN apk add --no-cache fontconfig ttf-dejavu freetype

COPY fonts/*.ttf /usr/share/fonts/truetype/custom/

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]