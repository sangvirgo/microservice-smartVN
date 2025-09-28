# Giai đoạn 1: Build ứng dụng bằng Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

# Đặt thư mục làm việc bên trong image
WORKDIR /app

# Sao chép các file cần thiết để tải dependency trước, tận dụng cache của Docker
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Cấp quyền thực thi cho mvnw
RUN chmod +x ./mvnw

# Tải dependencies
RUN ./mvnw dependency:go-offline

# Sao chép toàn bộ mã nguồn
COPY src ./src

# Build ứng dụng ra file .jar, bỏ qua các bài test
RUN ./mvnw clean install -DskipTests

# Giai đoạn 2: Tạo image cuối cùng để chạy ứng dụng
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Chỉ sao chép file .jar đã được build từ giai đoạn 'builder'
COPY --from=builder /app/target/*.jar app.jar

# Port sẽ được expose tùy theo service (xem bên dưới)
EXPOSE 8888

# Lệnh để khởi chạy ứng dụng khi container bắt đầu
ENTRYPOINT ["java", "-jar", "app.jar"]