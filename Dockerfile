FROM maven:3.9-amazoncorretto-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY checkstyle ./checkstyle

RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine

# Устанавливаем шрифты
RUN wget -q https://github.com/dejavu-fonts/dejavu-fonts/releases/download/version_2_37/dejavu-fonts-ttf-2.37.tar.bz2 && \
    tar -xjf dejavu-fonts-ttf-2.37.tar.bz2 && \
    mkdir -p /usr/share/fonts/truetype/dejavu && \
    cp dejavu-fonts-ttf-2.37/ttf/*.ttf /usr/share/fonts/truetype/dejavu/ && \
    rm -rf dejavu-fonts-ttf-2.37 dejavu-fonts-ttf-2.37.tar.bz2 && \
    apk add --no-cache fontconfig

COPY --from=build /app/target/*.jar /stat_voice_bot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/stat_voice_bot.jar"]