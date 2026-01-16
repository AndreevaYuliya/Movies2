# ===== BUILD =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем parent и pom'ы модулей (чтобы Maven видел multi-module структуру)
COPY pom.xml ./pom.xml
COPY email-api/pom.xml ./email-api/pom.xml
COPY email-service/pom.xml ./email-service/pom.xml
COPY movies2-app/pom.xml ./movies2-app/pom.xml

# Копируем исходники email-service
COPY email-api/src ./email-api/src
COPY email-service/src ./email-service/src

# Собираем модуль email-service
RUN mvn -q -pl email-service -am -DskipTests package

# ===== RUN =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/email-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
