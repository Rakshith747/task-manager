# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy frontend source (needed by frontend-maven-plugin)
COPY frontend/ frontend/

# Copy backend source
COPY backend/ backend/

# Build the application (Maven will also build React via frontend-maven-plugin)
WORKDIR /app/backend
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE $PORT
CMD ["java", "-jar", "app.jar", "--server.port=${PORT}"]
