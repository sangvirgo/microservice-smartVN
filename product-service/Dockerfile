# Giai đoạn 1: Build ứng dụng bằng Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Sao chép các file cần thiết để tải dependency trước
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Cấp quyền thực thi cho mvnw
RUN chmod +x ./mvnw

# Tải dependencies (layer caching)
RUN ./mvnw dependency:go-offline

# Sao chép toàn bộ mã nguồn
COPY src ./src

# Build ứng dụng ra file .jar, bỏ qua test
RUN ./mvnw clean install -DskipTests

# Giai đoạn 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy jar từ builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8082 (port của product-service)
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]