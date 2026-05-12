# =============================================
# DOCKERFILE — FitTracker Backend (Spring Boot)
# Utilise le JAR pré-compilé localement
# =============================================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copie le JAR déjà compilé (mvn package fait en local)
COPY target/streetworkout-api-1.0.0.jar app.jar

# Expose le port Spring Boot
EXPOSE 8080

# Démarre l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
