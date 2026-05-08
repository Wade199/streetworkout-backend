# =============================================
# DOCKERFILE — FitTracker Backend (Spring Boot)
# À copier dans : streetworkout-backend/Dockerfile
# =============================================

# ── STAGE 1 : Build Maven ────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copie le pom.xml en premier (cache des dépendances Maven)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copie le code source
COPY src ./src

# Build sans les tests
RUN mvn package -DskipTests -q

# ── STAGE 2 : Runtime JRE léger ──────────────
FROM eclipse-temurin:17-jre-alpine AS production

WORKDIR /app

# Copie le JAR compilé
COPY --from=builder /app/target/streetworkout-api-1.0.0.jar app.jar

# Expose le port Spring Boot
EXPOSE 8080

# Démarre l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
