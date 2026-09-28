# TÀI LIỆU QUY CHUẨN REST API — BREWLITE (DoAnCNPM)
> **Phiên bản:** 1.0.0  
> **Base URL (Local):** `http://localhost:8081/api`  
> **Content-Type:** `application/json`  
> **Mục đích:** Bản giao ước API (API Contract) dùng chung cho nhóm Frontend & Backend để phát triển song song.

---

## MỤC LỤC
1. [Quy chuẩn định dạng Response chung](#1-quy-chuẩn-định-dạng-response-chung)
2. [Bảng mã trạng thái HTTP (Status Codes)](#2-bảng-mã-trạng-thái-http-status-codes)
3. [Chi tiết các Endpoint API](#3-chi-tiết-các-endpoint-api)
   - [Hệ thống & Kiểm tra (System & Health)](#1-hệ-thống--kiểm-tra-system--health)
   - [Xác thực & Tài khoản (Auth & Users)](#2-xác-thực--tài-khoản-auth--users)
   - [Danh mục đồ uống (Categories)](#3-danh-mục-đồ-uống-categories)
   - [Sản phẩm & Đồ uống (Products)](#4-sản-phẩm--đồ-uống-products)
   - [Cửa hàng chi nhánh (Stores)](#5-cửa-hàng-chi-nhánh-stores)
   - [Đơn hàng & Bán hàng (Orders)](#6-đơn-hàng--bán-hàng-orders)
   - [Mã giảm giá (Vouchers)](#7-mã-giảm-giá-vouchers)
4. [Ví dụ gọi API bằng JavaScript (Fetch & Axios)](#4-ví-dụ-gọi-api-bằng-javascript-fetch--axios)

---

## 1. Quy chuẩn định dạng Response chung

Mọi API của Backend đều trả về định dạng JSON nhất quán để Frontend dễ dàng xử lý.

### Phản hồi thành công (Success Response)
```json
{
  "success": true,
  "message": "Thao tác thành công",
  "data": { ... } // hoặc [ ... ] nếu là danh sách
}
```

### Phản hồi thất bại (Error Response)
```json
{
  "success": false,
  "status": 400,
  "message": "Dữ liệu gửi lên không hợp lệ",
  "errors": {
    "phone": "Số điện thoại không đúng định dạng",
    "email": "Email đã được sử dụng"
  }
}
```

---

## 2. Bảng mã trạng thái HTTP (Status Codes)

| Mã | Tên | Ý nghĩa sử dụng |
|---|---|---|
| **200** | OK | Truy vấn (GET) hoặc cập nhật (PUT) thành công |
| **201** | Created | Thêm mới dữ liệu (POST) thành công |
| **400** | Bad Request | Dữ liệu gửi lên sai định dạng, vi phạm validation |
| **401** | Unauthorized | Chưa đăng nhập hoặc Token hết hạn |
| **403** | Forbidden | Không có quyền truy cập chức năng này (VD: Khách hàng gọi API Admin) |
| **404** | Not Found | Không tìm thấy tài nguyên (ID không tồn tại) |
| **500** | Internal Server Error | Lỗi hệ thống server (SQL crash, lỗi code backend) |

---

## 3. Chi tiết các Endpoint API

### 1. Hệ thống & Kiểm tra (System & Health)

#### `GET /api/health`
Kiểm tra xem Backend server có đang hoạt động hay không.

- **Response (200 OK):**
```json
{
  "application": "DoAnCNPM",
  "status": "UP"
}
```

---

### 2. Xác thực & Tài khoản (Auth & Users)

#### `POST /api/auth/register`
Đăng ký tài khoản khách hàng mới.

- **Request Body:**
```json
{
  "username": "nguyenbao",
  "password": "Password@123",
  "fullName": "Nguyễn Quốc Bảo",
  "email": "bao@gmail.com",
  "phone": "0912345678"
}
```

- **Response (201 Created):**
```json
{
  "success": true,
  "message": "Đăng ký tài khoản thành công!",
  "data": {
    "id": 1,
    "username": "nguyenbao",
    "fullName": "Nguyễn Quốc Bảo",
    "role": "CUSTOMER"
  }
}
```

#### `POST /api/auth/login`
Đăng nhập vào hệ thống.

- **Request Body:**
```json
{
  "username": "nguyenbao",
  "password": "Password@123"
}
```

- **Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng nhập thành công!",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "nguyenbao",
      "fullName": "Nguyễn Quốc Bảo",
      "role": "CUSTOMER",
      "walletBalance": 150000.00
    }
  }
}
```

---

### 3. Danh mục đồ uống (Categories)

#### `GET /api/categories`
Lấy danh sách tất cả danh mục (Cà phê, Trà sữa, Bánh ngọt...).

- **Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Cà Phê Truyền Thống",
      "description": "Các món cà phê pha phin và pha máy",
      "displayOrder": 1,
      "active": true
    },
    {
      "id": 2,
      "name": "Trà Trái Cây & Trà Sữa",
      "description": "Thức uống thanh mát giải nhiệt",
      "displayOrder": 2,
      "active": true
    }
  ]
}
```

---

### 4. Sản phẩm & Đồ uống (Products)

#### `GET /api/products`
Lấy danh sách sản phẩm (có hỗ trợ lọc theo danh mục).

- **Query Parameters:**
  - `categoryId` *(tùy chọn)*: Lọc theo mã danh mục (VD: `?categoryId=1`)
  - `search` *(tùy chọn)*: Tìm kiếm theo tên sản phẩm (VD: `?search=latte`)

- **Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "sku": "CF-LATTE",
      "name": "Cà Phê Cốt Dừa Latte",
      "description": "Sự kết hợp giữa espresso và sữa dừa béo ngậy",
      "basePrice": 45000.00,
      "imageUrl": "/image/products/latte-cot-dua.png",
      "categoryId": 1,
      "isAvailable": true
    },
    {
      "id": 2,
      "sku": "TEA-PEACH",
      "name": "Trà Đào Cam Sả",
      "description": "Trà đào thơm nồng kết hợp lát sả tươi",
      "basePrice": 39000.00,
      "imageUrl": "/image/products/tra-dao.png",
      "categoryId": 2,
      "isAvailable": true
    }
  ]
}
```

#### `GET /api/products/{id}`
Lấy thông tin chi tiết một sản phẩm.

- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "sku": "CF-LATTE",
    "name": "Cà Phê Cốt Dừa Latte",
    "description": "Sự kết hợp giữa espresso và sữa dừa béo ngậy",
    "basePrice": 45000.00,
    "imageUrl": "/image/products/latte-cot-dua.png",
    "categoryId": 1,
    "isAvailable": true
  }
}
```

#### `POST /api/products` *(Yêu cầu quyền Quản lý/Admin)*
Thêm sản phẩm mới.

- **Request Body:**
```json
{
  "sku": "CF-BAC-XIU",
  "categoryId": 1,
  "name": "Bạc Xỉu Sữa Tươi",
  "description": "Thơm béo ngọt ngào phù hợp gu nhẹ",
  "basePrice": 35000.00,
  "imageUrl": "/image/products/bac-xiu.png"
}
```

- **Response (201 Created):**
```json
{
  "success": true,
  "message": "Thêm sản phẩm thành công!",
  "data": { "id": 3 }
}
```

---

### 5. Cửa hàng chi nhánh (Stores)

#### `GET /api/stores`
Lấy danh sách các cơ sở / chi nhánh quán cà phê.

- **Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "STR-Q1",
      "name": "BrewLite Chi Nhánh Quận 1",
      "address": "123 Nguyễn Thị Minh Khai, P. Bến Thành, Q.1",
      "phone": "02838123456",
      "openTime": "07:00:00",
      "closeTime": "22:30:00",
      "active": true
    }
  ]
}
```

---

### 6. Đơn hàng & Bán hàng (Orders)

#### `POST /api/orders`
Tạo đơn đặt đồ uống mới.

- **Request Body:**
```json
{
  "storeId": 1,
  "customerId": 1,
  "voucherCode": "GIAM20K",
  "orderType": "DINE_IN", // DINE_IN, TAKE_AWAY, DELIVERY
  "paymentMethod": "WALLET", // WALLET, CASH, BANK_TRANSFER
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "size": "L",
      "iceLevel": 50,
      "sugarLevel": 70,
      "note": "Ít ngọt, nhiều đá"
    }
  ]
}
```

- **Response (201 Created):**
```json
{
  "success": true,
  "message": "Đặt hàng thành công!",
  "data": {
    "orderCode": "ORD-20260928-001",
    "subtotal": 90000.00,
    "discountAmount": 20000.00,
    "finalAmount": 70000.00,
    "status": "PAID",
    "createdAt": "2026-09-28T14:30:00"
  }
}
```

---

### 7. Mã giảm giá (Vouchers)

#### `GET /api/vouchers/check?code=GIAM20K&orderValue=90000`
Kiểm tra mã giảm giá có hợp lệ hay không.

- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "code": "GIAM20K",
    "discountType": "FIXED_AMOUNT",
    "discountValue": 20000.00,
    "isApplicable": true,
    "message": "Áp dụng mã giảm 20.000đ thành công!"
  }
}
```

---

## 4. Ví dụ gọi API bằng JavaScript (Fetch & Axios)

Dành cho bạn làm **Frontend** copy trực tiếp vào code:

### Cách 1: Dùng `fetch` chuẩn của trình duyệt
```javascript
const API_BASE_URL = "http://localhost:8081/api";

// 1. Lấy danh sách sản phẩm
async function loadProducts(categoryId = "") {
    try {
        const url = categoryId 
            ? `${API_BASE_URL}/products?categoryId=${categoryId}` 
            : `${API_BASE_URL}/products`;
            
        const res = await fetch(url);
        const result = await res.json();
        
        if (result.success) {
            console.log("Danh sách sản phẩm:", result.data);
            return result.data;
        }
    } catch (err) {
        console.error("Lỗi mạng:", err);
    }
}

// 2. Tạo đơn hàng
async function createOrder(orderPayload) {
    try {
        const res = await fetch(`${API_BASE_URL}/orders`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(orderPayload)
        });

        const result = await res.json();
        if (res.ok) {
            alert(result.message);
        } else {
            alert("Lỗi đặt hàng: " + (result.message || "Vui lòng thử lại"));
        }
    } catch (err) {
        console.error("Lỗi khi gửi đơn:", err);
    }
}
```
