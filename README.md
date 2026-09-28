# DoAnCNPM — BrewLite

Backend Spring Boot cho đồ án công nghệ phần mềm BrewLite.

## Quick Start — Windows

```powershell
# 1. Đi tới đúng thư mục dự án và kéo code mới nhất về
cd D:\CongNghePhanMem\DoAnCNPM
git pull origin main

# 2. Chạy ứng dụng (tự động tải thư viện và khởi động)
.\mvnw.cmd spring-boot:run
```

Sau khi ứng dụng khởi động, truy cập `http://localhost:8081/api/health` để kiểm tra.

## Công nghệ sử dụng

- Java 17;
- Spring Boot 3.4.4;
- Maven Wrapper 3.9.9;
- Spring Web;
- Spring JDBC;
- Bean Validation;
- Microsoft SQL Server Driver.

## 1. Yêu cầu trước khi chạy

Máy cần cài **JDK 17 trở lên**. Kiểm tra bằng lệnh:

```powershell
java -version
```

Nếu máy không nhận lệnh `java`, hãy cài JDK và thiết lập biến môi trường:

```text
JAVA_HOME=C:\Program Files\Java\jdk-17
Path=%JAVA_HOME%\bin
```

Đóng terminal cũ, mở terminal mới và chạy lại `java -version`.

> Dự án đã có Maven Wrapper nên không cần tải hoặc cài Maven toàn cục.

## 2. Cập nhật mã nguồn

Mở terminal tại thư mục dự án và chạy:

```powershell
git pull origin main
```

Sau khi pull, thư mục gốc phải có các file sau:

```text
.mvn/
mvnw
mvnw.cmd
pom.xml
```

Không xóa hoặc đưa các file trên vào `.gitignore` vì đây là Maven Wrapper dùng chung cho cả nhóm.

## 3. Thiết lập Maven Wrapper

### Windows

```powershell
.\mvnw.cmd -version
```

### macOS/Linux

```bash
chmod +x mvnw
./mvnw -version
```

Lần chạy đầu tiên, Maven Wrapper sẽ tự tải Maven 3.9.9 và các thư viện trong `pom.xml`. Vì vậy máy cần kết nối Internet. Các lần chạy sau sẽ sử dụng thư viện đã lưu trong bộ nhớ đệm Maven.

Nếu thành công, kết quả sẽ hiển thị các thông tin tương tự:

```text
Apache Maven 3.9.9
Java version: 17 hoặc mới hơn
```

## 4. Tải dependency và kiểm tra dự án

Windows:

```powershell
.\mvnw.cmd clean test
```

macOS/Linux:

```bash
./mvnw clean test
```

Thiết lập thành công khi cuối terminal xuất hiện:

```text
BUILD SUCCESS
```

## 5. Chạy ứng dụng

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Ứng dụng chạy tại cổng `8081`. Kiểm tra bằng trình duyệt hoặc Postman:

```http
GET http://localhost:8081/api/health
```

Kết quả mong đợi:

```json
{
  "application": "DoAnCNPM",
  "status": "UP"
}
```

Nhấn `Ctrl + C` trong terminal để dừng ứng dụng.

## 6. Mở dự án trong IntelliJ IDEA

1. Chọn **Open** và mở thư mục chứa `pom.xml`.
2. Nếu IntelliJ hỏi, chọn **Load Maven Project**.
3. Chọn **Project SDK** là JDK 17 hoặc mới hơn.
4. Trong phần Maven settings, chọn Maven Wrapper của dự án thay vì Maven cài toàn cục.
5. Đợi IntelliJ tải và đánh chỉ mục dependency xong rồi chạy `DoAnCnpmApplication`.

Nếu IDE chưa nhận dependency, mở cửa sổ Maven và chọn **Reload All Maven Projects**.

## 7. Cấu trúc package

```text
src/main/java/vn/doancnpm/
├── DoAnCnpmApplication.java
├── config/
├── controller/
├── domain/
├── repository/
└── service/
```

Khi tạo lớp mới, đặt lớp bên dưới package gốc `vn.doancnpm`. Ví dụ:

```java
package vn.doancnpm.service;
```

Không tạo lại các package ở ngoài `vn.doancnpm`, nếu không Spring có thể không tự động quét được component.

## 8. Trạng thái kết nối SQL Server

Dự án đã có Spring JDBC và Microsoft SQL Server Driver nhưng đang tạm tắt tự cấu hình `DataSource`. Nhờ vậy mọi thành viên có thể chạy ứng dụng trước khi thiết lập database.

Cấu hình tạm thời nằm trong:

```text
src/main/resources/application.properties
```

Các script SQL hiện có nằm tại:

```text
src/main/resources/db/
```

Khi nhóm bắt đầu tích hợp database, cần thống nhất thông tin kết nối SQL Server trước khi xóa cấu hình tạm dừng `DataSource`.

## 9. Lệnh Maven thường dùng

| Mục đích | Windows | macOS/Linux |
|---|---|---|
| Kiểm tra phiên bản | `.\mvnw.cmd -version` | `./mvnw -version` |
| Biên dịch | `.\mvnw.cmd compile` | `./mvnw compile` |
| Chạy kiểm thử | `.\mvnw.cmd test` | `./mvnw test` |
| Xóa kết quả build và kiểm thử | `.\mvnw.cmd clean test` | `./mvnw clean test` |
| Chạy Spring Boot | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| Đóng gói file JAR | `.\mvnw.cmd clean package` | `./mvnw clean package` |

File JAR sau khi đóng gói nằm trong thư mục `target/`.

## 10. Lỗi thường gặp

### `mvn` is not recognized

Không chạy `mvn`. Hãy dùng Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

### `JAVA_HOME is not defined correctly`

Kiểm tra `JAVA_HOME` phải trỏ đến thư mục JDK, không phải thư mục `bin` và không phải JRE.

### `Permission denied: ./mvnw`

Trên macOS/Linux:

```bash
chmod +x mvnw
```

### Không tải được dependency

Kiểm tra kết nối Internet, proxy hoặc tường lửa. Sau đó chạy lại:

```powershell
.\mvnw.cmd clean test
```

### Cổng 8081 đang được sử dụng

Tắt ứng dụng đang chiếm cổng hoặc tạm chạy bằng cổng khác:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8091"
```

## Quy trình kiểm tra trước khi đẩy code

```powershell
git pull origin main
.\mvnw.cmd clean test
git status
```

Chỉ push khi kiểm thử kết thúc với `BUILD SUCCESS` và không commit thư mục `target/`.