# Stage 1: Build with Maven + JDK 21
FROM maven@sha256:c3c9d3ac4ce8431a3995c0318b8d390f448e693dd4fabc16e9b68d2e1f3d7b46 AS builder

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


ARG CACHEBUST=1
# 3. Копируем исходный код только для нужных модулей
COPY common/src common/src
COPY portfolio-service/src portfolio-service/src

# 4. Собираем проект
RUN mvn clean package -DskipTests -pl portfolio-service -am


# Stage 2: Runtime (только JRE)
FROM eclipse-temurin@sha256:6ad8ed080d9be96b61438ec3ce99388e294af216ed57356000c06070e85c5d5d

WORKDIR /app

# Копируем готовый JAR
COPY --from=builder /app/portfolio-service/target/*.jar app.jar

EXPOSE 8082 9082

ENTRYPOINT ["java", "-jar", "app.jar"]