# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper và dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Cấp quyền thực thi
RUN chmod +x ./mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application (skip tests)
RUN ./mvnw clean install -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy jar từ builder
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8083 (order-service port)
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]