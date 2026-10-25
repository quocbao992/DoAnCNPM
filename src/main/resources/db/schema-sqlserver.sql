USE brewlite;
GO

CREATE TABLE stores (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(30) NOT NULL UNIQUE,
    name NVARCHAR(150) NOT NULL,
    address NVARCHAR(500) NOT NULL,
    phone NVARCHAR(20) NULL,
    open_time TIME NOT NULL DEFAULT '07:00:00',
    close_time TIME NOT NULL DEFAULT '22:00:00',
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE user_accounts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(100) NOT NULL,
    full_name NVARCHAR(120) NOT NULL,
    email NVARCHAR(120) NULL UNIQUE,
    phone NVARCHAR(20) NULL UNIQUE,
    role NVARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'BARISTA', 'CASHIER', 'STORE_MANAGER', 'ADMIN')),
    wallet_balance DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (wallet_balance >= 0), -- Ví điện tử BrewLite
    avatar_url NVARCHAR(500) NULL,
    enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
CREATE INDEX ix_users_phone ON user_accounts(phone);
CREATE INDEX ix_users_role ON user_accounts(role);

CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500) NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BIT NOT NULL DEFAULT 1
);

CREATE TABLE products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sku NVARCHAR(30) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(1000) NULL,
    image_url NVARCHAR(500) NULL,
    base_price DECIMAL(15,2) NOT NULL CHECK (base_price >= 0), 
    is_available BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
CREATE INDEX ix_products_category ON products(category_id, is_available);

CREATE TABLE vouchers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(30) NOT NULL UNIQUE,
    name NVARCHAR(150) NOT NULL,
    discount_type NVARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED_AMOUNT')),
    discount_value DECIMAL(15,2) NOT NULL CHECK (discount_value > 0),
    min_order_value DECIMAL(15,2) NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(15,2) NULL,
    start_date DATETIME2 NOT NULL,
    end_date DATETIME2 NOT NULL,
    usage_limit INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    active BIT NOT NULL DEFAULT 1,
    CONSTRAINT ck_voucher_dates CHECK (end_date >= start_date)
);

CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_code NVARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES user_accounts(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    voucher_id BIGINT NULL REFERENCES vouchers(id),
    order_type NVARCHAR(20) NOT NULL DEFAULT 'PICKUP' CHECK (order_type IN ('PICKUP', 'DINE_IN', 'DELIVERY')),
    status NVARCHAR(30) NOT NULL CHECK (status IN (
        'PENDING_PAYMENT',   
        'PAID',              
        'PREPARING',         
        'READY_FOR_PICKUP',  
        'COMPLETED',         
        'CANCELLED'          
    )),
    subtotal DECIMAL(15,2) NOT NULL CHECK (subtotal >= 0),
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount DECIMAL(15,2) NOT NULL CHECK (total_amount >= 0),
    note NVARCHAR(500) NULL,
    pickup_time DATETIME2 NULL, 
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    completed_at DATETIME2 NULL,
    cancelled_at DATETIME2 NULL,
    cancel_reason NVARCHAR(500) NULL
);
CREATE INDEX ix_orders_customer ON orders(customer_id, created_at DESC);
CREATE INDEX ix_orders_store_status ON orders(store_id, status, created_at DESC);

CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name_snapshot NVARCHAR(150) NOT NULL,
    product_image NVARCHAR(500) NULL,
    size_name NVARCHAR(10) NOT NULL DEFAULT 'M' CHECK (size_name IN ('S', 'M', 'L')),
    sugar_level NVARCHAR(20) NOT NULL DEFAULT '100%' CHECK (sugar_level IN ('0%', '30%', '50%', '70%', '100%')),
    ice_level NVARCHAR(20) NOT NULL DEFAULT '100%' CHECK (ice_level IN ('NO_ICE', '30%', '50%', '100%')),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(15,2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(15,2) NOT NULL CHECK (subtotal >= 0),
    special_instructions NVARCHAR(300) NULL
);
CREATE INDEX ix_order_items_order ON order_items(order_id);

CREATE TABLE cashless_payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
    payment_method NVARCHAR(30) NOT NULL CHECK (payment_method IN ('BREWLITE_WALLET', 'VNPAY', 'MOMO', 'BANK_QR', 'CREDIT_CARD')),
    payment_status NVARCHAR(20) NOT NULL CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_code NVARCHAR(100) NOT NULL UNIQUE,
    gateway_response NVARCHAR(1000) NULL,
    paid_at DATETIME2 NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
CREATE INDEX ix_payments_order ON cashless_payments(order_id);
CREATE INDEX ix_payments_status ON cashless_payments(payment_status);

CREATE TABLE wallet_transactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(id),
    transaction_type NVARCHAR(20) NOT NULL CHECK (transaction_type IN ('TOPUP', 'PAY_ORDER', 'REFUND')),
    amount DECIMAL(15,2) NOT NULL,
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description NVARCHAR(500) NULL,
    reference_order_id BIGINT NULL REFERENCES orders(id),
    transaction_code NVARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
CREATE INDEX ix_wallet_user_created ON wallet_transactions(user_id, created_at DESC);
GO
