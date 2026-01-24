# 1. Budowanie aplikacji (Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Budujemy wszystko, pomijając testy dla szybkości (w dockerze)
RUN mvn clean package -DskipTests

# 2. Uruchamianie aplikacji (JDK)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Kopiujemy zbudowany plik JAR z poprzedniego etapu
# Ścieżka zależy od struktury modułów. Zakładam, że app/target/app-0.0.1-SNAPSHOT.jar
COPY --from=build /app/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]