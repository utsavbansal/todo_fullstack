# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ARG JAR_FILE=/app/target/todo-backend-1.0.0.jar
COPY --from=build ${JAR_FILE} app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
