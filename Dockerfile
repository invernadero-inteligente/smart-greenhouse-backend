FROM maven:3.9-eclipse-temurin-21 AS app

WORKDIR /app

COPY invernadero_inteligente_backend/pom.xml .
RUN mvn dependency:go-offline

COPY invernadero_inteligente_backend/src ./src
COPY invernadero_inteligente_backend/.env ./.env

RUN echo "===== VERIFICANDO MIGRACIONES EN RESOURCES =====" && \
    find src/main/resources -type f || true

RUN test -f src/main/resources/db/migration/V1__init_schema.sql

RUN mvn clean package -DskipTests -Dspring.flyway.enabled=false

RUN echo "===== VERIFICANDO CONTENIDO DEL JAR =====" && \
    jar tf target/*.jar | grep -E "db/migration|flyway" || true

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "echo '===== CONTENIDO FLYWAY EN JAR ====='; jar tf target/*.jar | grep -E 'db/migration|flyway' || true; echo '===== INICIANDO APP ====='; java -jar target/*.jar --debug"]