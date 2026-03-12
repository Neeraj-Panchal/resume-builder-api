# Build stage (Maven with Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run stage (Lightweight Java 21 JRE)
# 'jr-alpine' ki jagah 'jre-alpine' ya 'jdk-alpine' use hota hai
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]