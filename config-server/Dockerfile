# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy dependency files first
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# ✅ Cache Maven dependencies với BuildKit
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copy source
COPY src ./src

# ✅ Build với cached dependencies
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean install -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
