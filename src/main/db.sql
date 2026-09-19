-- =========================================
-- LAUNDRY MANAGEMENT SYSTEM (CLEAN POS DB)
-- =========================================

CREATE DATABASE IF NOT EXISTS fx_laundry;
USE fx_laundry;

-- =========================================
-- USERS (LOGIN)
-- =========================================
CREATE TABLE fx_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','STAFF') DEFAULT 'STAFF',
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- CUSTOMERS
-- =========================================
CREATE TABLE fx_customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- SERVICE CATEGORIES
-- =========================================
CREATE TABLE fx_service_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_active TINYINT DEFAULT 1
);

-- =========================================
-- SERVICE TYPES
-- =========================================
CREATE TABLE fx_service_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_active TINYINT DEFAULT 1
);

-- =========================================
-- SERVICES
-- =========================================
CREATE TABLE fx_services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT,
    image VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES fx_service_categories(id)
);

-- =========================================
-- PRODUCTS
-- =========================================

CREATE TABLE fx_service_prices (
    id INT PRIMARY KEY AUTO_INCREMENT,
    service_id INT NOT NULL,
    type_id INT NOT NULL,
    price DOUBLE NOT NULL,

    FOREIGN KEY (service_id) REFERENCES fx_services(id),
    FOREIGN KEY (type_id) REFERENCES fx_service_types(id)
);

-- =========================================
-- PRODUCT CATEGORIES
-- =========================================
CREATE TABLE fx_product_categories (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    is_active TINYINT DEFAULT 1
);
-- =========================================
-- PRODUCTS
-- =========================================
CREATE TABLE fx_products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    image VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    barcode VARCHAR(100) UNIQUE,
    category_id INT NOT NULL,
    description TEXT,
    cost_price DOUBLE DEFAULT 0,
    price DOUBLE NOT NULL,
    stock INT DEFAULT 0,
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES fx_product_categories(id)
);

-- =========================================
-- ORDERS
-- =========================================
CREATE TABLE fx_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_date DATE,
    status ENUM('RECEIVED','WASHING','READY','DELIVERED') DEFAULT 'RECEIVED',
    subtotal DOUBLE DEFAULT 0,
    discount DOUBLE DEFAULT 0,
    total DOUBLE DEFAULT 0,
    paid_amount DOUBLE DEFAULT 0,
    due_amount DOUBLE DEFAULT 0,

    FOREIGN KEY (customer_id) REFERENCES fx_customers(id)
);

-- =========================================
-- ORDER ITEMS (SERVICES + PRODUCTS)
-- SNAPSHOT PRICING INCLUDED
-- =========================================
CREATE TABLE fx_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    item_type ENUM('SERVICE','PRODUCT') NOT NULL,
    item_id INT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    unit_price DOUBLE NOT NULL,
    quantity INT DEFAULT 1,
    total DOUBLE NOT NULL,

    FOREIGN KEY (order_id) REFERENCES fx_orders(id)
);

-- =========================================
-- PAYMENTS METHODS
-- =========================================
CREATE TABLE fx_payment_methods (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_active TINYINT DEFAULT 1
);

-- =========================================
-- PAYMENTS
-- =========================================
CREATE TABLE fx_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    method_id INT NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES fx_customers(id),
    FOREIGN KEY (method_id) REFERENCES fx_payment_methods(id)
);

-- =========================================
-- EXPENSE CATEGORIES
-- =========================================
CREATE TABLE fx_expense_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    is_active TINYINT DEFAULT 1
);
-- =========================================
-- EXPENSES
-- =========================================
CREATE TABLE fx_expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    expense_date DATE NOT NULL,
    method_id INT NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (category_id) REFERENCES fx_expense_categories(id),
    FOREIGN KEY (method_id) REFERENCES fx_payment_methods(id)
);

-- =========================================
-- SETTINGS
-- =========================================
CREATE TABLE fx_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================
-- DEFAULT USERS
-- password = admin123
-- =========================================
INSERT INTO fx_users
(name, username, password, role)
VALUES
    ('Administrator', 'admin', 'admin123', 'ADMIN');

-- =========================================
-- DEFAULT WALK-IN CUSTOMER
-- =========================================
INSERT INTO fx_customers
(name, phone, email, address)
VALUES
    ('Walk-in Customer', '', '', '');

-- =========================================
-- DEFAULT SERVICE CATEGORIES
-- =========================================
INSERT INTO fx_service_categories (name)
VALUES
    ('Men'),
    ('Women'),
    ('Kids'),
    ('Household');

-- =========================================
-- DEFAULT SERVICE TYPES
-- =========================================
INSERT INTO fx_service_types (name)
VALUES
    ('Wash'),
    ('Iron'),
    ('Dry Clean'),
    ('Wash & Iron');

-- =========================================
-- DEFAULT SERVICES
-- =========================================
INSERT INTO fx_services
(category_id, image, name)
VALUES
    (1, '', 'Shirt'),
    (1, '', 'Pant'),
    (2, '', 'Saree'),
    (2, '', 'Dress'),
    (3, '', 'School Uniform'),
    (4, '', 'Blanket');

-- =========================================
-- DEFAULT SERVICE PRICES
-- =========================================

-- Shirt
INSERT INTO fx_service_prices
(service_id, type_id, price)
VALUES
    (1, 1, 50),
    (1, 2, 30),
    (1, 3, 80),
    (1, 4, 70);

-- Pant
INSERT INTO fx_service_prices
(service_id, type_id, price)
VALUES
    (2, 1, 60),
    (2, 2, 35),
    (2, 3, 90),
    (2, 4, 80);

-- Saree
INSERT INTO fx_service_prices
(service_id, type_id, price)
VALUES
    (3, 1, 120),
    (3, 2, 60),
    (3, 3, 180),
    (3, 4, 150);

-- Dress
INSERT INTO fx_service_prices
(service_id, type_id, price)
VALUES
    (4, 1, 100),
    (4, 2, 50),
    (4, 3, 160),
    (4, 4, 130);

-- School Uniform
INSERT INTO fx_service_prices
(service_id, type_id, price)
VALUES
    (5, 1, 70),
    (5, 2, 40),
    (5, 3, 100),
    (5, 4, 90);

-- Blanket
INSERT INTO fx_service_prices
(service_id, type_id, price)
VALUES
    (6, 1, 200),
    (6, 3, 300);

-- =========================================
-- DEFAULT PRODUCT CATEGORIES
-- =========================================
INSERT INTO fx_product_categories (name)
VALUES
    ('Cleaning'),
    ('Packaging'),
    ('Accessories'),
    ('Laundry Supplies');

-- =========================================
-- DEFAULT PRODUCTS
-- =========================================
INSERT INTO fx_products
(image, name, barcode, category_id, description, cost_price, price, stock)
VALUES

    -- Cleaning
    ('', 'Detergent Powder', '100001', 1,
     'Laundry detergent powder', 80, 120, 50),
    ('', 'Liquid Detergent', '100002', 1,
     'Premium liquid detergent', 150, 220, 30),
    ('', 'Fabric Softener', '100003', 1,
     'Softener for clothes', 100, 150, 25),
    ('', 'Bleach', '100004', 1,
     'White cloth bleach', 60, 90, 20),

    -- Packaging
    ('', 'Laundry Bag', '200001', 2,
     'Reusable laundry bag', 30, 50, 100),
    ('', 'Plastic Cover', '200002', 2,
     'Transparent packaging cover', 5, 10, 500),
    ('', 'Premium Packaging', '200003', 2,
     'Premium delivery packaging', 20, 40, 80),

    -- Accessories
    ('', 'Hanger', '300001', 3,
     'Plastic clothes hanger', 10, 20, 200),
    ('', 'Perfume Spray', '300002', 3,
     'Cloth perfume spray', 40, 70, 40),

    -- Laundry Supplies
    ('', 'Stain Remover', '400001', 4,
     'Strong stain remover', 90, 140, 15),
    ('', 'Brush', '400002', 4,
     'Laundry cleaning brush', 25, 45, 35);

-- =========================================
-- DEFAULT PAYMENT METHODS
-- =========================================
INSERT INTO fx_payment_methods (name)
VALUES
    ('Cash'),
    ('Card'),
    ('Bkash'),
    ('Nagad'),
    ('Rocket'),
    ('Bank Transfer');

-- =========================================
-- DEFAULT EXPENSE CATEGORIES
-- =========================================
INSERT INTO fx_expense_categories (name)
VALUES
    ('Electricity'),
    ('Water Bill'),
    ('Salary'),
    ('Detergent Purchase'),
    ('Transport'),
    ('Maintenance');

-- =========================================
-- DEFAULT SETTINGS
-- =========================================
INSERT INTO fx_settings
(setting_key, setting_value, description)
VALUES
    ('shop_name', 'FX Laundry', 'Laundry shop name'),
    ('shop_phone', '01700000000', 'Shop phone number'),
    ('shop_address', 'Dhaka, Bangladesh', 'Shop address'),
    ('currency', '৳', 'Currency symbol'),
    ('invoice_prefix', 'INV-', 'Invoice prefix'),
    ('default_language', 'en', 'System language'),
    ('tax_id', '00000000', 'Tax ID Number'),
    ('tax_name', 'VAT', 'Default tax name'),
    ('tax_percentage', '0', 'Default tax percentage'),
    ('terms_condition', 'No return after 7 days.', 'Default terms and conditions');


-- =========================================
-- VIEW CUSTOMER BALANCE
-- =========================================
CREATE VIEW fx_view_customer_balances AS
SELECT
    c.*,
    COALESCE((SELECT SUM(total) FROM fx_orders WHERE customer_id = c.id), 0) AS total_orders,
    COALESCE((SELECT SUM(p.amount) FROM fx_payments p
                                            JOIN fx_orders o ON p.customer_id = c.id
              WHERE o.customer_id = c.id), 0) AS total_payments
FROM fx_customers c;
