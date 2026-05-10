FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests -Dspring.flyway.enabled=false

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd -ms /bin/bash spring

COPY --from=builder /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]