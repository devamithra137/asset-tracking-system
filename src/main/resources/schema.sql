DROP DATABASE IF EXISTS asset_tracking_db;
CREATE DATABASE asset_tracking_db;
USE asset_tracking_db;

-- ==========================================================
-- 1. TABLE CREATION (FULLY FIXED)
-- ==========================================================

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('USER', 'ADMINISTRATOR', 'INVENTORY_MANAGER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Assets Table
CREATE TABLE IF NOT EXISTS assets (
    asset_id INT PRIMARY KEY AUTO_INCREMENT,
    asset_name VARCHAR(100) NOT NULL,
    asset_tag VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    purchase_date DATE,
    purchase_cost DECIMAL(10,2),
    current_value DECIMAL(10,2),
    status ENUM('AVAILABLE', 'ASSIGNED', 'MAINTENANCE', 'RETIRED') DEFAULT 'AVAILABLE',
    location VARCHAR(100),
    assigned_to INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_to) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Asset Requests Table
CREATE TABLE IF NOT EXISTS asset_requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    request_type ENUM('ASSIGNMENT', 'RETURN', 'MAINTENANCE') NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    approved_by INT NULL,
    approved_date TIMESTAMP NULL,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Asset History Table
CREATE TABLE IF NOT EXISTS asset_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    asset_id INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by INT NOT NULL,
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================================
-- 2. RESET & SAMPLE DATA
-- ==========================================================

-- ✅ RESET EXISTING DATA
SET FOREIGN_KEY_CHECKS = 0; 
TRUNCATE TABLE asset_history;
TRUNCATE TABLE asset_requests;
TRUNCATE TABLE assets;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ✅ INSERT USERS
INSERT INTO users (username, password, full_name, email, role) VALUES
('admin', 'admin123', 'System Administrator', 'admin@company.com', 'ADMINISTRATOR'),
('manager1', 'manager123', 'John Manager', 'manager@company.com', 'INVENTORY_MANAGER'),
('manager2', 'manager123', 'Sarah Manager', 'sarah.manager@company.com', 'INVENTORY_MANAGER'),

-- ✅ 22 Normal Users
('user1', 'user123', 'Alice Johnson', 'alice1@company.com', 'USER'),
('user2', 'user123', 'Bob Thomas', 'bob2@company.com', 'USER'),
('user3', 'user123', 'Charles Miller', 'charles3@company.com', 'USER'),
('user4', 'user123', 'Diana Smith', 'diana4@company.com', 'USER'),
('user5', 'user123', 'Ethan Brown', 'ethan5@company.com', 'USER'),
('user6', 'user123', 'Fiona Davis', 'fiona6@company.com', 'USER'),
('user7', 'user123', 'George Wilson', 'george7@company.com', 'USER'),
('user8', 'user123', 'Hannah Moore', 'hannah8@company.com', 'USER'),
('user9', 'user123', 'Ian Taylor', 'ian9@company.com', 'USER'),
('user10', 'user123', 'Jenny White', 'jenny10@company.com', 'USER'),
('user11', 'user123', 'Kevin Harris', 'kevin11@company.com', 'USER'),
('user12', 'user123', 'Laura Martin', 'laura12@company.com', 'USER'),
('user13', 'user123', 'Mike Scott', 'mike13@company.com', 'USER'),
('user14', 'user123', 'Nora Lewis', 'nora14@company.com', 'USER'),
('user15', 'user123', 'Oscar Young', 'oscar15@company.com', 'USER'),
('user16', 'user123', 'Paula King', 'paula16@company.com', 'USER'),
('user17', 'user123', 'Quinn Baker', 'quinn17@company.com', 'USER'),
('user18', 'user123', 'Ruby Adams', 'ruby18@company.com', 'USER'),
('user19', 'user123', 'Steve Clark', 'steve19@company.com', 'USER'),
('user20', 'user123', 'Tina Hall', 'tina20@company.com', 'USER'),
('user21', 'user123', 'Umar Khan', 'umar21@company.com', 'USER'),
('user22', 'user123', 'Vera Patel', 'vera22@company.com', 'USER'),
('user23', 'user123', 'Will Turner', 'will23@company.com', 'USER'),
('user24', 'user123', 'Xavier Silva', 'xavier24@company.com', 'USER'),
('user25', 'user123', 'Yana Rose', 'yana25@company.com', 'USER');

-- ✅ INSERT 100 ASSETS
INSERT INTO assets (asset_name, asset_tag, category, description, purchase_date, purchase_cost, current_value, status, location) VALUES
-- Laptops
('Dell Latitude 5500', 'LAP001', 'Laptop', 'Business laptop', '2023-01-15', 900.00, 800.00, 'AVAILABLE', 'IT Store'),
('Dell XPS 15', 'LAP002', 'Laptop', 'i7, 16GB RAM', '2023-01-20', 1500.00, 1400.00, 'ASSIGNED', 'User Desk'),
('HP EliteBook 840', 'LAP003', 'Laptop', 'Core i5, 8GB RAM', '2023-02-10', 1000.00, 900.00, 'AVAILABLE', 'IT Store'),
('MacBook Pro M1', 'LAP004', 'Laptop', 'Apple M1 16GB', '2023-03-05', 2000.00, 1800.00, 'ASSIGNED', 'User Desk'),
('Lenovo ThinkPad T14', 'LAP005', 'Laptop', 'AMD Ryzen 7', '2023-03-18', 1200.00, 1100.00, 'AVAILABLE', 'IT Store'),

-- Monitors
('Dell 24-inch Monitor', 'MON001', 'Monitor', '1080p LED', '2023-02-22', 180.00, 150.00, 'AVAILABLE', 'IT Store'),
('HP 27-inch 4K', 'MON002', 'Monitor', '4K UHD Monitor', '2023-02-25', 350.00, 290.00, 'AVAILABLE', 'IT Store'),
('Samsung Curved Monitor', 'MON003', 'Monitor', '32-inch curved', '2023-03-15', 400.00, 350.00, 'ASSIGNED', 'User Desk'),

-- Phones
('iPhone 13', 'PHN001', 'Mobile', '128GB Blue', '2023-03-10', 999.00, 900.00, 'ASSIGNED', 'Employee'),
('Samsung S22', 'PHN002', 'Mobile', '128GB Black', '2023-03-11', 950.00, 850.00, 'AVAILABLE', 'IT Store'),
('OnePlus 10', 'PHN003', 'Mobile', '128GB Red', '2023-03-12', 850.00, 750.00, 'AVAILABLE', 'IT Store'),

-- Printers
('HP LaserJet Pro', 'PRT001', 'Printer', 'Laser printer', '2023-04-01', 400.00, 350.00, 'AVAILABLE', 'Office Supplies'),
('Canon Pixma G3000', 'PRT002', 'Printer', 'Ink Tank Printer', '2023-04-02', 250.00, 200.00, 'MAINTENANCE', 'Service Center'),

-- Desktops
('Dell OptiPlex 7090', 'DTP001', 'Desktop', 'Core i5, 8GB RAM', '2023-05-01', 800.00, 750.00, 'AVAILABLE', 'IT Store'),
('HP ProDesk 400', 'DTP002', 'Desktop', 'Core i7, 16GB RAM', '2023-05-05', 1000.00, 900.00, 'ASSIGNED', 'User Desk'),

-- NETWORK DEVICES
('Cisco Router 2901', 'NET001', 'Router', 'Enterprise router', '2023-05-15', 1200.00, 1100.00, 'AVAILABLE', 'Server Room'),

-- ✅ 83 More random assets to reach 100
-- Auto generate laptop tags LAP006 to LAP050
-- ✅ Auto generate laptop tags LAP006 to LAP050
INSERT INTO assets (asset_name, asset_tag, category, description, purchase_date, purchase_cost, current_value, status, location)
SELECT 
    CONCAT('Dell Laptop ', x.num), 
    CONCAT('LAP', LPAD(x.num,3,'0')),
    'Laptop',
    'Auto generated sample laptop',
    '2023-02-01',
    900.00,
    800.00,
    IF(x.num % 3 = 0, 'ASSIGNED', 'AVAILABLE'),
    'Warehouse'
FROM
(
 SELECT 6 AS num UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
 UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
 UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
 UNION SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION SELECT 45
 UNION SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION SELECT 50
) AS x;


-- ✅ INSERT SAMPLE REQUESTS
INSERT INTO asset_requests (user_id, asset_id, request_type, status) VALUES
(4, 1, 'ASSIGNMENT', 'PENDING'),
(5, 2, 'RETURN', 'PENDING'),
(6, 3, 'MAINTENANCE', 'APPROVED'),
(7, 4, 'RETURN', 'REJECTED'),
(8, 5, 'ASSIGNMENT', 'PENDING');

