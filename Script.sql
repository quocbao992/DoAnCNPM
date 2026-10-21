INSERT INTO customers (id, date_of_birth, gender, loyalty_point, phone_number, user_id) 
VALUES (1, '2000-01-01', 1, 0, '0901234567', 1);


INSERT INTO category (id, display_order, name, status) 
VALUES (1, 1, 'Đồ uống', 1);

INSERT INTO product (id, img_url, name, status, stock, unit_price, category_id, version) 
VALUES (1, 'https://example.com/trasua.jpg', 'Trà sữa trân châu', 1, 100, 35000, 1, 1);

UPDATE product 
SET stock = 100 
WHERE id = 1;



INSERT INTO cart_items (id, cart_id, product_id, quantity, size, unit_price, note, created_at, updated_at) 
VALUES (1, 1, 1, 2, 'M', 35000, 'Ít đá', NOW(), NOW());


INSERT INTO carts (id, status, customer_id, created_at, updated_at) 
VALUES (1, 1, 1, NOW(), NOW());

-- 1. Tạo Topping mẫu trong bảng toppings (nếu chưa có)
INSERT INTO topping (id, name, status,unit_price) 
VALUES (1, 'Trân châu trắng', 1, 5000);

INSERT INTO cart_item_toppings (id, price, quantity, cart_item_id, topping_id) 
VALUES (1, 5000, 1, 1, 1);


-- Tạm tắt kiểm tra khóa ngoại để dọn dữ liệu thoải mái không sợ lỗi
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Dọn dẹp sạch sẽ dữ liệu cũ
DELETE FROM cart_item_toppings;
DELETE FROM cart_items;
DELETE FROM carts;
DELETE FROM order_item_toppings;
DELETE FROM order_items;
DELETE FROM orders;
DELETE From inventory_log;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

-- 2. Thiết lập lại số lượng tồn kho ban đầu cho sản phẩm (id = 1) là 100
UPDATE product 
SET stock = 100 
WHERE id = 1;

-- 3. Tạo giỏ hàng mới cho khách hàng (customer_id = 1)
INSERT INTO carts (id, status, customer_id, created_at, updated_at) 
VALUES (1, 1, 1, NOW(), NOW());

-- 4. Thêm sản phẩm vào giỏ hàng (cart_items)
INSERT INTO cart_items (id, cart_id, product_id, quantity, size, unit_price, note, created_at, updated_at) 
VALUES (1, 1, 1, 2, 'M', 35000, 'Ít đá', NOW(), NOW());

-- 5. Thêm Topping vào món trong giỏ hàng (cart_item_toppings)
INSERT INTO cart_item_toppings (id, price, quantity, cart_item_id, topping_id) 
VALUES (1, 5000, 1, 1, 1);
