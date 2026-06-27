# --- Build stage: compile the Spring Boot jar with the Maven wrapper ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline || true
COPY src ./src
RUN ./mvnw -q -B clean package -DskipTests

# --- Runtime stage: small JRE image running the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
