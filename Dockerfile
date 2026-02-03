FROM maven:3.9-amazoncorretto-17 AS build

WORKDIR /app

# 1. Копируем только pom.xml сначала (кэширование зависимостей)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Копируем остальное
COPY src ./src
COPY checkstyle ./checkstyle

# 3. Собираем с оптимизацией памяти
ENV MAVEN_OPTS="-Xmx512m -Xms256m"
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -q

FROM amazoncorretto:17-alpine

# 4. Устанавливаем шрифты САМЫМ БЫСТРЫМ способом
# Используем конкретные версии пакетов чтобы избежать поиска
RUN apk add --no-cache --repository=http://dl-cdn.alpinelinux.org/alpine/v3.18/main \
    fontconfig=2.14.2-r0 \
    ttf-dejavu=2.37-r3

COPY --from=build /app/target/*.jar /app.jar

EXPOSE 8080

# 5. Оптимизация Java для 2 ГБ RAM
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseSerialGC -XX:+UseStringDeduplication"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]