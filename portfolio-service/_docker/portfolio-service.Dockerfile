# Stage 1: Build with Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 1. Копируем ВСЕ POM файлы
COPY pom.xml .
COPY common/pom.xml common/
COPY api-gateway/pom.xml api-gateway/
COPY portfolio-service/pom.xml portfolio-service/
COPY trading-service/pom.xml trading-service/
COPY quotes-service/pom.xml quotes-service/
COPY auth-service/pom.xml auth-service/

# 2. Скачиваем зависимости
RUN mvn dependency:go-offline -B

# 3. Копируем исходный код только для нужных модулей
COPY common/src common/src
COPY portfolio-service/src portfolio-service/src

# 4. Собираем проект
RUN mvn clean package -DskipTests -pl portfolio-service -am


# Stage 2: Runtime (только JRE)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем готовый JAR
COPY --from=builder /app/portfolio-service/target/*.jar app.jar

EXPOSE 8082 9082

ENTRYPOINT ["java", "-jar", "app.jar"]