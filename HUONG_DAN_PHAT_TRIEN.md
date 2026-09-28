
---

## MỤC LỤC
1. [Tổng quan Kiến trúc & Nguyên lý hoạt động](#1-tổng-quan-kiến-trúc--nguyên-lý-hoạt-động)
2. [Bản chất Spring JDBC & Cách dùng `JdbcTemplate`](#2-bản-chất-spring-jdbc--cách-dùng-jdbctemplate)
3. [Chi tiết các tầng & Code mẫu chuẩn](#3-chi-tiết-các-tầng--code-mẫu-chuẩn)
   - [Tầng 1: Domain & DTO (Dữ liệu)](#tầng-1-domain--dto-data-transfer-object)
   - [Tầng 2: Repository (Truy vấn CSDL)](#tầng-2-repository-truy-vấn-csdl)
   - [Tầng 3: Service (Nghiệp vụ ứng dụng)](#tầng-3-service-nghiệp-vụ-ứng-dụng)
   - [Tầng 4: Controller (Giao tiếp API)](#tầng-4-controller-giao-tiếp-api)
   - [Tầng 5: Xử lý lỗi tập trung (Global Exception Handling)](#tầng-5-xử-lý-lỗi-tập-trung-global-exception-handling)
4. [Quy trình Phối hợp Song song Frontend & Backend](#4-quy-trình-phối-hợp-song-song-frontend--backend)
   - [Giai đoạn 1: Thống nhất API Contract (Bản hợp đồng API)](#giai-đoạn-1-thống-nhất-api-contract-bản-hợp-đồng-api)
   - [Giai đoạn 2: Phát triển độc lập (Frontend Mocking & Backend Testing)](#giai-đoạn-2-phát-triển-độc-lập)
   - [Giai đoạn 3: Cấu hình CORS để ghép nối](#giai-đoạn-3-cấu-hình-cors-để-ghép-nối)
   - [Giai đoạn 4: Ghép nối & Kiểm thử tích hợp](#giai-đoạn-4-ghép-nối--kiểm-thử-tích-hợp)
5. [Quy tắc Git & Làm việc nhóm](#5-quy-tắc-git--làm-việc-nhóm)

---

## 1. Tổng quan Kiến trúc & Nguyên lý hoạt động

Dự án áp dụng mô hình **3-Tier Architecture (Kiến trúc 3 tầng)** chuẩn công nghiệp:

```text
[ Trình duyệt / Client (Frontend) ]
                 │
                 ▼  (HTTP Request: JSON qua REST API)
┌───────────────────────────────────────────────┐
│              CONTROLLER LAYER                 │
│  - Nhận HTTP Request (GET, POST, PUT, DELETE) │
│  - Validate dữ liệu đầu vào (@Valid)          │
│  - Trả về HTTP Response (JSON + HTTP Status)  │
└──────────────────────┬────────────────────────┘
                       ▼
┌───────────────────────────────────────────────┐
│                SERVICE LAYER                  │
│  - Xử lý logic nghiệp vụ (Tính toán, kiểm tra)│
│  - Quản lý giao dịch (@Transactional)         │
│  - Cầu nối trung gian giữa Controller và Repo │
└──────────────────────┬────────────────────────┘
                       ▼
┌───────────────────────────────────────────────┐
│               REPOSITORY LAYER                │
│  - Viết câu lệnh SQL trực tiếp                │
│  - Dùng JdbcTemplate để thực thi              │
│  - Ánh xạ ResultSet thành Java Object         │
└──────────────────────┬────────────────────────┘
                       ▼
            [ Microsoft SQL Server ]
```

### Tại sao lại chia thành nhiều tầng?
- **Tách biệt trách nhiệm (Separation of Concerns):** Mỗi tầng làm đúng một việc. Controller không được viết SQL, Repository không xử lý giao diện.
- **Dễ bảo trì và kiểm thử (Testability):** Có thể viết Unit Test cho từng hàm Service mà không cần bật cơ sở dữ liệu.
- **Làm việc nhóm không giẫm chân lên nhau:** Người phụ trách DB viết Repository, người phụ trách logic viết Service, người làm giao tiếp viết Controller.

---

## 2. Bản chất Spring JDBC & Cách dùng `JdbcTemplate`

Khác với Spring Data JPA (dùng Hibernate tự động sinh câu truy vấn SQL), **Spring JDBC** cho phép lập trình viên **tự tay viết các câu lệnh SQL thuần**.

### Ưu điểm của Spring JDBC:
1. **Kiểm soát 100% câu truy vấn:** Bạn biết chính xác câu SQL nào được gửi vào SQL Server, tối ưu chỉ mục (Index) và hiệu năng cực tốt.
2. **Không bị lỗi ngầm:** Không gặp các lỗi kinh điển của JPA như N+1 Query, LazyInitializationException, Detached Entity.
3. **Model là POJO thuần túy:** Không cần bất kỳ annotation phức tạp nào như `@Entity`, `@Table`, `@ManyToOne`...

### Các phương thức cốt lõi của `JdbcTemplate`:
- `jdbcTemplate.query(sql, rowMapper, params...)`: Dùng cho câu lệnh `SELECT` trả về danh sách nhiều dòng (`List<T>`).
- `jdbcTemplate.queryForObject(sql, rowMapper, params...)`: Dùng cho `SELECT` trả về duy nhất 1 dòng (nếu không có sẽ văng lỗi `EmptyResultDataAccessException`).
- `jdbcTemplate.update(sql, params...)`: Dùng cho các câu lệnh `INSERT`, `UPDATE`, `DELETE` (trả về số dòng bị ảnh hưởng).

---

## 3. Chi tiết các tầng & Code mẫu chuẩn

Lấy ví dụ chức năng: **Quản lý Khách Hàng (Customer)**.

### Tầng 1: Domain & DTO (Data Transfer Object)

- **Domain (`Customer.java`):** Đại diện cho bảng trong Database.
- **DTO (`CustomerRequest.java`):** Đại diện cho dữ liệu Frontend gửi lên (có gắn các annotation kiểm tra hợp lệ validation).

```java
// File: src/main/java/vn/doancnpm/domain/Customer.java
package vn.doancnpm.domain;

public class Customer {
    private Long id;
    private String name;
    private String phone;
    private String email;

    public Customer() {}

    public Customer(Long id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Getter và Setter đầy đủ
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

```java
// File: src/main/java/vn/doancnpm/domain/dto/CustomerRequest.java
package vn.doancnpm.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerRequest {
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String name;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    // Getter và Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

---

### Tầng 2: Repository (Truy vấn CSDL)

Chỉ có 3 annotation quan trọng:
- `@Repository`: Đánh dấu bean tầng CSDL.
- `@Autowired`: Tiêm `JdbcTemplate`.
- `@Transactional`: Đảm bảo toàn vẹn dữ liệu khi ghi (Insert/Update/Delete).

```java
// File: src/main/java/vn/doancnpm/repository/CustomerRepository.java
package vn.doancnpm.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.doancnpm.domain.Customer;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper dùng chung để chuyển đổi từ SQL ResultSet sang Java Object
    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> new Customer(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email")
    );

    // 1. Lấy tất cả khách hàng
    public List<Customer> findAll() {
        String sql = "SELECT id, name, phone, email FROM customers ORDER BY id DESC";
        return jdbcTemplate.query(sql, customerRowMapper);
    }

    // 2. Tìm theo ID (dùng Optional để tránh lỗi NullPointerException)
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT id, name, phone, email FROM customers WHERE id = ?";
        try {
            Customer customer = jdbcTemplate.queryForObject(sql, customerRowMapper, id);
            return Optional.ofNullable(customer);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // 3. Thêm mới khách hàng
    public int save(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, customer.getName(), customer.getPhone(), customer.getEmail());
    }

    // 4. Cập nhật khách hàng
    public int update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ? WHERE id = ?";
        return jdbcTemplate.update(sql, customer.getName(), customer.getPhone(), customer.getEmail(), customer.getId());
    }

    // 5. Xóa khách hàng theo ID
    public int deleteById(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
```

> [!CAUTION]
> **Quy tắc an toàn bảo mật:** Luôn dùng dấu `?` trong câu lệnh SQL để truyền tham số. Tuyệt đối **KHÔNG ĐƯỢC** cộng chuỗi (ví dụ: `WHERE id = " + id`) vì sẽ bị tấn công **SQL Injection**!

---

### Tầng 3: Service (Nghiệp vụ ứng dụng)

- `@Service`: Đánh dấu lớp xử lý logic.
- `@Transactional`: Đặt ở đây để nếu có 1 bước lỗi thì toàn bộ thao tác trong hàm sẽ tự động rollback.

```java
// File: src/main/java/vn/doancnpm/service/CustomerService.java
package vn.doancnpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.doancnpm.domain.Customer;
import vn.doancnpm.domain.dto.CustomerRequest;
import vn.doancnpm.repository.CustomerRepository;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
    }

    @Transactional
    public void createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());

        customerRepository.save(customer);
    }
}
```

---

### Tầng 4: Controller (Giao tiếp API)

- `@RestController`: Đánh dấu Controller trả về dữ liệu dạng JSON.
- `@RequestMapping("/api/customers")`: Định nghĩa đường dẫn gốc cho nhóm API này.
- `@Valid`: Kích hoạt việc kiểm tra các quy tắc trong `CustomerRequest` (nếu vi phạm sẽ tự động trả lỗi 400).

```java
// File: src/main/java/vn/doancnpm/controller/CustomerController.java
package vn.doancnpm.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.doancnpm.domain.Customer;
import vn.doancnpm.domain.dto.CustomerRequest;
import vn.doancnpm.service.CustomerService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // GET: http://localhost:8081/api/customers
    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // GET: http://localhost:8081/api/customers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // POST: http://localhost:8081/api/customers
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody CustomerRequest request) {
        customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thêm khách hàng thành công!"));
    }
}
```

---

### Tầng 5: Xử lý lỗi tập trung (Global Exception Handling)

Nếu dữ liệu gửi lên sai hoặc có lỗi xảy ra, thay vì để Spring Boot trả về mã lỗi 500 kèm một đống text Java khó đọc, ta dùng `@RestControllerAdvice` để chuyển toàn bộ lỗi thành định dạng JSON chuẩn:

```java
// File: src/main/java/vn/doancnpm/config/GlobalExceptionHandler.java
package vn.doancnpm.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý lỗi validation (@NotBlank, @Email...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    // Xử lý các lỗi nghiệp vụ thông thường
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
```

---

## 4. Quy trình Phối hợp Song song Frontend & Backend

Nhiều nhóm sinh viên hay bị tình trạng: **"Đợi Backend làm xong hết thì Frontend mới bắt đầu làm"**. Đây là cách làm rất chậm và dễ trễ deadline.

Quy trình chuẩn công nghiệp là **Frontend và Backend làm việc song song cùng một lúc** theo 4 bước sau:

```text
       [ BƯỚC 1: HỌP THỐNG NHẤT API CONTRACT ]
                         │
         ┌───────────────┴───────────────┐
         ▼                               ▼
    [ FRONTEND ]                    [ BACKEND ]
  Dùng Mock Data                  Viết Code Repository,
  để dựng giao diện,              Service, Controller
  hiệu ứng, bắt sự kiện           và test bằng Postman
         │                               │
         └───────────────┬───────────────┘
                         ▼
       [ BƯỚC 3: CẤU HÌNH CORS TRÊN BACKEND ]
                         │
                         ▼
  [ BƯỚC 4: GHÉP NỐI (INTEGRATION) & TEST THỰC TẾ ]
```

---

### Giai đoạn 1: Thống nhất API Contract (Bản hợp đồng API)

Trước khi viết bất kỳ dòng code nào, 2 bạn (Frontend và Backend) phải ngồi lại thống nhất với nhau:
1. **Endpoint URL là gì?**
2. **Method là gì?** (`GET`, `POST`, `PUT`, `DELETE`)
3. **Frontend gửi lên cái gì (Request Body JSON)?**
4. **Backend trả về cái gì (Response JSON)?**

**Ví dụ viết tài liệu API mẫu trao đổi giữa 2 bên:**
- **URL:** `POST /api/customers`
- **Request Body (Frontend gửi lên):**
  ```json
  {
    "name": "Nguyễn Văn A",
    "phone": "0912345678",
    "email": "vana@gmail.com"
  }
  ```
- **Response thành công (201 Created):**
  ```json
  {
    "message": "Thêm khách hàng thành công!"
  }
  ```
- **Response lỗi (400 Bad Request):**
  ```json
  {
    "status": 400,
    "errors": {
      "phone": "Số điện thoại không hợp lệ"
    }
  }
  ```

---

### Giai đoạn 2: Phát triển độc lập

Khi đã có bản API Contract ở trên:

#### Việc của bạn Frontend:
- Không cần đợi Backend chạy server!
- Bạn tự tạo một file dữ liệu giả (`mock-data.json`) hoặc một hàm giả lập:
  ```javascript
  // File: js/api.js (Mock data tạm thời phía Frontend)
  async function fetchCustomers() {
      // Giả lập trả về dữ liệu mẫu khớp với hợp đồng API
      return [
          { id: 1, name: "Nguyễn Văn A", phone: "0912345678", email: "a@gmail.com" },
          { id: 2, name: "Trần Thị B", phone: "0987654321", email: "b@gmail.com" }
      ];
  }
  ```
- Tiến hành thiết kế giao diện HTML/CSS, dựng bảng hiển thị, làm modal thêm mới, viết validate form trên Javascript.

#### Việc của bạn Backend:
- Tạo bảng trong SQL Server.
- Viết Repository $\rightarrow$ Service $\rightarrow$ Controller.
- Dùng **Postman** hoặc công cụ **Thunder Client** trong VS Code để tự test các API xem đã trả về đúng định dạng JSON như hợp đồng chưa.

---

### Giai đoạn 3: Cấu hình CORS để ghép nối

Khi Frontend chạy ở một cổng (ví dụ Live Server cổng `5500` hoặc React cổng `3000`) mà gọi sang Backend cổng `8081`, trình duyệt sẽ chặn lại vì lý do bảo mật (**Lỗi CORS - Cross-Origin Resource Sharing**).

Backend cần thêm file cấu hình sau để cho phép Frontend truy cập:

```java
// File: src/main/java/vn/doancnpm/config/CorsConfig.java
package vn.doancnpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // Cho phép mọi nguồn (hoặc điền http://127.0.0.1:5500)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
```

---

### Giai đoạn 4: Ghép nối & Kiểm thử tích hợp

Khi cả 2 bên đã hoàn thành phần việc của mình:
1. Bạn Frontend chỉ việc đổi đường link từ hàm Mock sang link thật:
   ```javascript
   const API_BASE_URL = "http://localhost:8081/api";

   async function getCustomers() {
       const response = await fetch(`${API_BASE_URL}/customers`);
       return await response.json();
   }
   ```
2. Bật cả Frontend và Backend lên để kiểm tra:
   - Thêm mới một khách hàng trên giao diện $\rightarrow$ kiểm tra xem trong SQL Server đã xuất hiện dòng mới chưa.
   - Nhập sai số điện thoại $\rightarrow$ kiểm tra xem giao diện có hiện thông báo lỗi màu đỏ do Backend trả về không.

---

## 5. Quy tắc Git & Làm việc nhóm

Để không bao giờ bị dính Conflict hoặc mất code của nhau:

1. **Tuyệt đối không code trực tiếp trên nhánh `main`:**
   - Mỗi thành viên làm việc trên nhánh của riêng mình (ví dụ `QuocBao`, `TienDat`, `MaiPhuong`...).
2. **Quy trình làm việc hàng ngày:**
   - **Bắt đầu ngày mới:** Chuyển về `main` và lấy code mới nhất về:
     ```powershell
     git checkout main
     git pull origin main
     ```
   - **Chuyển sang nhánh của mình để làm việc:**
     ```powershell
     git checkout QuocBao
     git merge main   # Đồng bộ code mới nhất của main vào nhánh mình
     ```
   - **Làm xong chức năng thì commit và push lên nhánh của mình:**
     ```powershell
     git add .
     git commit -m "Hoàn thành chức năng thêm khách hàng"
     git push origin QuocBao
     ```
   - **Tạo Pull Request (PR) trên GitHub** để nhóm trưởng hoặc các thành viên khác duyệt trước khi merge vào `main`.

---
*Tài liệu được biên soạn phục vụ cho đồ án công nghệ phần mềm — DoAnCNPM (BrewLite).*
