# Stage 1: Build with Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 1. Копируем ВЕСЬ проект (включая все модули)
COPY . .

# 2. Устанавливаем только зависимости (без компиляции) — это кэшируется
#    --also-make гарантирует, что common будет собран и установлен в .m2
RUN mvn dependency:resolve -DskipTests --projects common --also-make

# 3. Теперь собираем целевой сервис
RUN mvn clean package -DskipTests --projects api-gateway --also-make

# Stage 2: Runtime (только JRE)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем готовый JAR
COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8081 9091

ENTRYPOINT ["java", "-jar", "app.jar"]