# 🚀 TechShop - Backend

Chào mừng bạn đến với dự án backend của **TechShop**\! Đây là một hệ thống thương mại điện tử hoàn chỉnh được xây dựng trên nền tảng Spring Boot, cung cấp các API mạnh mẽ để quản lý sản phẩm, đơn hàng, người dùng và tích hợp thanh toán.

## 🏁 Bắt đầu

Để chạy dự án trên máy cục bộ của bạn, hãy làm theo các bước được hướng dẫn chi tiết dưới đây.

### ✅ Yêu cầu cài đặt

Trước khi bắt đầu, hãy đảm bảo bạn đã cài đặt các công cụ sau:

  * **Java Development Kit (JDK)** - `v21` hoặc cao hơn
  * **Apache Maven** - `v3.9` hoặc cao hơn
  * **MySQL Server** - `v8.0` hoặc cao hơn
  * Một IDE Java như **IntelliJ IDEA** hoặc **VS Code**

### 🛠️ Hướng dẫn cài đặt

Làm theo các bước sau để thiết lập và chạy dự án.

#### Bước 1: Clone Repository

Mở terminal của bạn và clone repository về máy:

```bash
git clone https://github.com/WEBSITE-QU-N-LI-BAN-HANG-CONG-NGH/BACKEND
cd BACKEND
```

#### Bước 2: Thiết lập Cơ sở dữ liệu

Dự án sử dụng MySQL để lưu trữ dữ liệu. Tệp `Script_Database.sql` đã bao gồm mọi thứ bạn cần, từ việc tạo cơ sở dữ liệu `ecommerce` cho đến các bảng và dữ liệu mẫu cần thiết.

##### **Cách 1: Sử dụng Command Line (Khuyên dùng)**

1.  Mở terminal hoặc Command Prompt.

2.  Điều hướng đến thư mục gốc của dự án backend.

3.  Chạy lệnh sau (thay `your_username` bằng tên người dùng MySQL của bạn):

    ```bash
    mysql -u your_username -p ecommerce < ./Script_Database.sql
    ```

4.  Nhập mật khẩu MySQL của bạn khi được yêu cầu.

##### **Cách 2: Sử dụng Công cụ GUI (MySQL Workbench, DBeaver)**

1.  Kết nối tới MySQL Server của bạn bằng công cụ GUI.
2.  Mở tệp `Script_Database.sql` có trong thư mục dự án.
3.  Sao chép toàn bộ nội dung của tệp.
4.  Dán vào một cửa sổ truy vấn mới trong công cụ GUI.
5.  Thực thi (Run) toàn bộ script để tạo database và các bảng.


#### Bước 3: Cấu hình `application.properties`

Đây là bước quan trọng nhất. Tạo một tệp mới có tên `application.properties` trong thư mục `src/main/resources/`.

Sao chép toàn bộ nội dung dưới đây và dán vào tệp vừa tạo. Sau đó, **thay thế các giá trị có dạng `your_...`** bằng thông tin cấu hình của bạn.

```properties
server.port=8080

# API Prefix
api.prefix=/api/v1

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_shop
spring.datasource.username=root
spring.datasource.password=your_key
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
#spring.jpa.show-sql=true
#spring.jpa.properties.hibernate.format_sql=true
#spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=true

# JWT Configuration
auth.token.jwtSecret=your_key
auth.token.accessExpirationInMils=3600000999
auth.token.refreshExpirationInMils=864000000

# Cookie Configuration
app.useSecureCookie=false

# OAuth2 Configuration
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=your_key
spring.security.oauth2.client.registration.google.client-secret=your_key
spring.security.oauth2.client.registration.google.scope=email,profile

# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=your_key
spring.security.oauth2.client.registration.github.client-secret=your_key
spring.security.oauth2.client.registration.github.scope=user:email

app.oauth2.redirectUri=http://localhost:5173/oauth2/redirect
app.oauth2.failureRedirectUri=http://localhost:5173/login

# OTP Configuration
app.otp.expiration-minutes=10
app.otp.resend-cooldown-minutes=2

# CORS Configuration
cors.allowed-origins=http://localhost:5173,http://localhost:5174,http://localhost:5175

# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_key
spring.mail.password=your_key
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging Configuration
logging.level.org.springframework.security=DEBUG
logging.level.com.webanhang.team_project=DEBUG

# VNPAY Configuration
vnpay.tmn-code=your_key
vnpay.hash-secret=your_key
vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:5173/payment/result

# Cloudinary Configuration
cloudinary.cloudName=your_cloudinary_cloud_name
cloudinary.apiKey=your_cloudinary_api_key
cloudinary.apiSecret=your_cloudinary_api_secret
cloudinary.apiSecure=true

app.company.logo.url=https://res.cloudinary.com/dgygvrrjs/image/upload/v1745387610/ChatGPT_Image_Apr_5_2025_12_08_58_AM_ociguu.png?fbclid=IwY2xjawJ4KxJleHRuA2FlbQIxMABicmlkETFnbUszR1o2RlZrQXJ2VFRXAR7SKjjUPYQHQovx3wZg3p14ksqpKnPTakahujkwPCwl21n8F7-sQJX0fXLfRg_aem_ghKIYi2m6VITMUEzqoiUOg

# contact info
contact.info.phone = +1111111111
contact.info.email = sonvtthanhthanh@gmail.com
contact.info.address = 212F2/11 Nguyen Huu Canh, Phuong Thang Nhat, TP Vung Tau, Ba Ria - Vung Tau
contact.info.businessHours = 8:00 - 22:00, th? Hai - Ch? Nh?t
contact.info.facebook = https://www.facebook.com/techshop
contact.info.instagram = https://www.instagram.com/techshop
contact.info.youtube = https://youtube.com/techshop
```

> ⚠️ **Lưu ý quan trọng về Mật khẩu ứng dụng Gmail:**
> Để gửi email (OTP, thông báo), bạn cần bật "Xác minh 2 bước" cho tài khoản Google và tạo một **"Mật khẩu ứng dụng"** riêng. **Không sử dụng mật khẩu đăng nhập thông thường của bạn** cho `spring.mail.password`.

#### Bước 4: Build và Chạy ứng dụng

Sau khi hoàn tất cấu hình, bạn có thể khởi động máy chủ bằng một trong hai cách sau:

##### **Cách 1: Sử dụng Maven (Khuyên dùng)**

Mở terminal tại thư mục gốc của dự án và chạy lệnh:

```bash
mvn spring-boot:run
```

Maven sẽ tự động tải các dependency cần thiết, build và khởi chạy ứng dụng.

##### **Cách 2: Chạy từ IDE**

1.  Mở dự án trong IDE của bạn (IntelliJ, VS Code, ...).
2.  Tìm và mở tệp `TeamProjectApplication.java`.
3.  Nhấn nút `Run` hoặc `Debug` bên cạnh phương thức `main`.

-----

## 🗺️ Cấu trúc API

Tất cả các API đều có tiền tố là `/api/v1`.

  * `/api/v1/auth/**`: Các API liên quan đến xác thực (đăng nhập, đăng ký, OTP,...).
  * `/api/v1/admin/**`: Các API dành cho quản trị viên.
  * `/api/v1/seller/**`: Các API dành cho người bán.
  * `/api/v1/customer/**`: Các API dành cho khách hàng.
  * `/api/v1/products/**`: Các API chung để truy vấn sản phẩm.
  * `/api/v1/categories/**`: Các API chung để truy vấn danh mục.
  * `/api/v1/cart/**`: Các API quản lý giỏ hàng.
  * `/api/v1/orders/**`: Các API quản lý đơn hàng.
  * `/api/v1/payment/**`: Các API xử lý thanh toán.

