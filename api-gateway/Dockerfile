# Giai đoạn 1: Build ứng dụng bằng Maven
# Sử dụng một image JDK đầy đủ để biên dịch code
FROM eclipse-temurin:17-jdk-jammy as builder

# Đặt thư mục làm việc bên trong image
WORKDIR /app

# Sao chép các file cần thiết để tải dependency trước, tận dụng cache của Docker
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Sao chép toàn bộ mã nguồn
COPY src ./src

# Build ứng dụng ra file .jar, bỏ qua các bài test
RUN ./mvnw clean install -DskipTests


# Giai đoạn 2: Tạo image cuối cùng để chạy ứng dụng
# Sử dụng một image JRE nhỏ hơn để tiết kiệm dung lượng
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Chỉ sao chép file .jar đã được build từ giai đoạn 'builder'
COPY --from=builder /app/target/*.jar app.jar

# Lệnh để khởi chạy ứng dụng khi container bắt đầu
ENTRYPOINT ["java", "-jar", "app.jar"]
