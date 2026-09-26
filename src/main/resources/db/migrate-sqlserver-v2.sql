USE brewlite;
GO

DELETE FROM wallet_transactions;
DELETE FROM cashless_payments;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM vouchers;
DELETE FROM products;
DELETE FROM categories;
DELETE FROM user_accounts;
DELETE FROM stores;
GO

INSERT INTO stores (code, name, address, phone, open_time, close_time) VALUES
(N'ST-Q1', N'BrewLite Bến Nghé - Quận 1', N'12 Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM', '02838112233', '07:00:00', '22:30:00'),
(N'ST-BT', N'BrewLite Landmark 81', N'Tầng B1, TTTM Landmark 81, Bình Thạnh, TP.HCM', '02838445566', '07:30:00', '22:00:00'),
(N'ST-TD', N'BrewLite Khu Đô Thị ĐHQG', N'Đường Quảng Trường Sáng Tạo, TP. Thủ Đức, TP.HCM', '02838778899', '06:30:00', '21:30:00');

INSERT INTO user_accounts (username, password_hash, full_name, email, phone, role, wallet_balance) VALUES
(N'admin', N'$2a$10$e8K7b1a0...adminhash', N'Quản trị viên BrewLite', N'admin@brewlite.vn', '0901000001', 'ADMIN', 1000000),
(N'barista_tuan', N'$2a$10$e8K7b1a0...baristahash', N'Trần Anh Tuấn (Barista)', N'tuan.tran@brewlite.vn', '0901000002', 'BARISTA', 0),
(N'cashier_huy', N'$2a$10$e8K7b1a0...cashierhash', N'Nguyễn Nhật Huy (Thu ngân)', N'huy.nguyen@brewlite.vn', '0901000003', 'CASHIER', 0),
(N'khachhang1', N'$2a$10$e8K7b1a0...customerhash', N'Phạm Quốc Bảo', N'bao.pham@gmail.com', '0909123456', 'CUSTOMER', 250000),
(N'khachhang2', N'$2a$10$e8K7b1a0...customerhash', N'Lê Tấn Tài', N'tai.le@gmail.com', '0909654321', 'CUSTOMER', 85000);

INSERT INTO categories (name, description, display_order) VALUES
(N'Cà Phê Truyền Thống', N'Hạt Robusta Đắk Lắk đậm đà, phin truyền thống', 1),
(N'Cà Phê Pha Máy (Espresso)', N'Hạt Arabica Cầu Đất nguyên chất kết hợp sữa tươi', 2),
(N'Trà Trái Cây Tươi', N'Trà thanh nhiệt kết hợp trái cây tươi tự nhiên', 3),
(N'Cold Brew Ủ Lạnh', N'Cà phê ủ lạnh 18 tiếng êm dịu, ít đắng', 4),
(N'Bánh Ngọt & Croissant', N'Bánh tươi nướng trong ngày dùng kèm cà phê', 5);

INSERT INTO products (sku, category_id, name, description, base_price, is_available) VALUES
(N'CF-01', 1, N'Cà Phê Đen Đá Phin', N'Cà phê phin truyền thống đậm vị, thơm nồng hạt Robusta', 25000, 1),
(N'CF-02', 1, N'Cà Phê Sữa Đá Phin', N'Cà phê phin đậm đà hòa quyện cùng sữa đặc ngọt béo', 29000, 1),
(N'CF-03', 1, N'Bạc Xỉu Đá BrewLite', N'Nhiều sữa tươi ngọt dịu, hòa cùng một chút cà phê phin', 32000, 1),

(N'ESP-01', 2, N'Americano Đá', N'Espresso pha loãng với nước lọc mát lạnh, vị chua thanh', 35000, 1),
(N'ESP-02', 2, N'Caffe Latte', N'Espresso kết hợp sữa tươi thanh trùng đánh bọt mịn màng', 45000, 1),
(N'ESP-03', 2, N'Caramel Macchiato', N'Vị béo ngậy của sữa tươi, nồng nàn espresso và sốt caramel thơm lừng', 49000, 1),

(N'TEA-01', 3, N'Trà Đào Cam Sả', N'Trà đen ủ thơm mát cùng đào giòn, cam tươi và sả đập dập', 42000, 1),
(N'TEA-02', 3, N'Trà Vải Hoa Hồng', N'Vị thanh mát của trà oolong, hương hoa hồng tự nhiên và quả vải mọng nước', 42000, 1),

(N'CB-01', 4, N'Cold Brew Cam Vàng', N'Cà phê ủ lạnh 18h thơm hương thảo mộc kết hợp tép cam vàng tươi mát', 48000, 1),

(N'BAKE-01', 5, N'Croissant Bơ Tỏi Nướng', N'Bánh sừng bò ngàn lớp xốp giòn phủ xốt bơ tỏi thơm ngậy', 35000, 1);

INSERT INTO vouchers (code, name, discount_type, discount_value, min_order_value, max_discount_amount, start_date, end_date, usage_limit, used_count) VALUES
(N'BREWLITENEW', N'Giảm 20% đơn đầu tiên cho người dùng mới', 'PERCENT', 20, 30000, 20000, '2026-01-01', '2026-12-31', 1000, 12),
(N'CASHLESS5K', N'Giảm ngay 5.000đ khi thanh toán không tiền mặt', 'FIXED_AMOUNT', 5000, 30000, 5000, '2026-01-01', '2026-12-31', 500, 5);

INSERT INTO orders (order_code, customer_id, store_id, voucher_id, order_type, status, subtotal, discount_amount, total_amount, note, pickup_time) VALUES
(N'BL-20260926-001', 4, 1, 1, 'PICKUP', 'PAID', 74000, 14800, 59200, N'Lấy ít đá giùm mình', DATEADD(MINUTE, 20, SYSUTCDATETIME()));

INSERT INTO order_items (order_id, product_id, product_name_snapshot, size_name, sugar_level, ice_level, quantity, unit_price, subtotal) VALUES
(1, 2, N'Cà Phê Sữa Đá Phin', 'M', '70%', '50%', 1, 35000, 35000),
(1, 3, N'Bạc Xỉu Đá BrewLite', 'L', '100%', '50%', 1, 39000, 39000);

INSERT INTO cashless_payments (order_id, amount, payment_method, payment_status, transaction_code, paid_at) VALUES
(1, 59200, 'BREWLITE_WALLET', 'SUCCESS', N'TXN-WALLET-20260926-0001', SYSUTCDATETIME());

INSERT INTO wallet_transactions (user_id, transaction_type, amount, balance_before, balance_after, description, reference_order_id, transaction_code) VALUES
(4, 'TOPUP', 300000, 0, 300000, N'Nạp tiền vào ví BrewLite qua QR Ngân hàng', NULL, N'TOPUP-20260926-8888'),
(4, 'PAY_ORDER', -59200, 300000, 240800, N'Thanh toán đơn hàng cà phê #BL-20260926-001', 1, N'PAY-20260926-0001');

GO
