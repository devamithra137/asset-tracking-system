Create database example1;
use example1;
CREATE TABLE Pizza (
    pizza_id INT PRIMARY KEY,
    pizza_name VARCHAR(50),
    category VARCHAR(30),
    size VARCHAR(10),
    price DECIMAL(5,2),
    ingredients VARCHAR(200),
    calories INT,
    is_veg BOOLEAN,
    is_available BOOLEAN,
    preparation_time INT, -- in minutes
    rating DECIMAL(2,1),
    number_of_orders INT,
    launch_date DATE,
    restaurant_name VARCHAR(50),
    city VARCHAR(50)
);
INSERT INTO Pizza VALUES
(1,'Margherita','Classic','Small',99.00,'Cheese, Tomato',250,TRUE,TRUE,10,4.5,500,'2023-01-01','Pizza Hut','Chennai'),

(2,'Margherita','Classic','Medium',199.00,'Cheese, Tomato',400,TRUE,TRUE,12,4.6,800,'2023-01-01','Pizza Hut','Madurai'),

(3,'Margherita','Classic','Large',299.00,'Cheese, Tomato',600,TRUE,TRUE,15,4.7,1200,'2023-01-01','Pizza Hut','Coimbatore'),

(4,'Pepperoni','Non-Veg','Small',149.00,'Cheese, Pepperoni',300,FALSE,TRUE,12,4.4,600,'2023-02-01','Dominos','Chennai'),

(5,'Pepperoni','Non-Veg','Medium',249.00,'Cheese, Pepperoni',500,FALSE,TRUE,15,4.6,900,'2023-02-01','Dominos','Madurai'),

(6,'Pepperoni','Non-Veg','Large',349.00,'Cheese, Pepperoni',700,FALSE,TRUE,18,4.8,1400,'2023-02-01','Dominos','Trichy'),

(7,'Farmhouse','Veg','Small',129.00,'Onion, Capsicum, Mushroom',270,TRUE,TRUE,11,4.3,450,'2023-03-01','Dominos','Chennai'),

(8,'Farmhouse','Veg','Medium',229.00,'Onion, Capsicum, Mushroom',420,TRUE,TRUE,14,4.5,700,'2023-03-01','Dominos','Madurai'),

(9,'Farmhouse','Veg','Large',329.00,'Onion, Capsicum, Mushroom',650,TRUE,TRUE,17,4.7,1100,'2023-03-01','Dominos','Salem'),

(10,'BBQ Chicken','Non-Veg','Medium',279.00,'Chicken, BBQ Sauce',550,FALSE,TRUE,16,4.8,1300,'2023-04-01','Pizza Hut','Chennai'),

(11,'BBQ Chicken','Non-Veg','Large',379.00,'Chicken, BBQ Sauce',750,FALSE,TRUE,20,4.9,1600,'2023-04-01','Pizza Hut','Madurai'),

(12,'Veggie Deluxe','Veg','Small',119.00,'Tomato, Onion, Corn',260,TRUE,TRUE,10,4.2,400,'2023-05-01','KFC','Chennai'),

(13,'Veggie Deluxe','Veg','Medium',219.00,'Tomato, Onion, Corn',430,TRUE,TRUE,13,4.4,650,'2023-05-01','KFC','Madurai'),

(14,'Veggie Deluxe','Veg','Large',319.00,'Tomato, Onion, Corn',680,TRUE,TRUE,16,4.6,1000,'2023-05-01','KFC','Coimbatore'),

(15,'Paneer Pizza','Veg','Medium',259.00,'Paneer, Cheese',500,TRUE,TRUE,14,4.7,950,'2023-06-01','Pizza Hut','Madurai'),

(16,'Paneer Pizza','Veg','Large',359.00,'Paneer, Cheese',720,TRUE,TRUE,18,4.8,1250,'2023-06-01','Pizza Hut','Chennai'),

(17,'Chicken Supreme','Non-Veg','Medium',289.00,'Chicken, Onion',560,FALSE,TRUE,16,4.9,1500,'2023-07-01','Dominos','Madurai'),

(18,'Chicken Supreme','Non-Veg','Large',389.00,'Chicken, Onion',780,FALSE,TRUE,21,5.0,1800,'2023-07-01','Dominos','Chennai'),

(19,'Cheese Burst','Veg','Medium',269.00,'Extra Cheese',600,TRUE,TRUE,15,4.8,1400,'2023-08-01','Pizza Hut','Madurai'),

(20,'Cheese Burst','Veg','Large',369.00,'Extra Cheese',850,TRUE,TRUE,19,4.9,1700,'2023-08-01','Pizza Hut','Chennai');
CREATE TABLE Customer (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(50),
    phone VARCHAR(15),
    email VARCHAR(100),
    gender VARCHAR(10),
    age INT,
    city VARCHAR(50),
    registration_date DATE,
    is_active BOOLEAN,
    total_orders INT,
    total_spent DECIMAL(10,2),
    preferred_category VARCHAR(30),
    last_order_date DATE
);
INSERT INTO Customer VALUES
(1,'Arun Kumar','9876543210','arun@gmail.com','Male',22,'Madurai','2024-01-10',TRUE,15,4500.00,'Veg','2025-12-10'),

(2,'Priya Sharma','9876543211','priya@gmail.com','Female',21,'Chennai','2024-02-15',TRUE,10,3200.00,'Classic','2025-11-05'),

(3,'Rahul Singh','9876543212','rahul@gmail.com','Male',24,'Coimbatore','2024-03-20',TRUE,18,6000.00,'Non-Veg','2025-12-01'),

(4,'Sneha Reddy','9876543213','sneha@gmail.com','Female',23,'Madurai','2024-01-05',TRUE,8,2400.00,'Veg','2025-10-15'),

(5,'Karthik','9876543214','karthik@gmail.com','Male',25,'Salem','2024-04-01',TRUE,20,8000.00,'Non-Veg','2025-12-12'),

(6,'Divya','9876543215','divya@gmail.com','Female',22,'Trichy','2024-05-10',TRUE,12,3800.00,'Veg','2025-11-25'),

(7,'Vijay','9876543216','vijay@gmail.com','Male',26,'Chennai','2024-06-18',TRUE,25,9500.00,'Non-Veg','2025-12-15'),

(8,'Anitha','9876543217','anitha@gmail.com','Female',21,'Madurai','2024-07-12',TRUE,7,2100.00,'Classic','2025-10-10'),

(9,'Suresh','9876543218','suresh@gmail.com','Male',27,'Coimbatore','2024-08-22',TRUE,30,12000.00,'Non-Veg','2025-12-18'),

(10,'Meena','9876543219','meena@gmail.com','Female',23,'Chennai','2024-09-05',TRUE,14,4200.00,'Veg','2025-11-30'),

(11,'Ajay','9876543220','ajay@gmail.com','Male',24,'Madurai','2024-10-10',TRUE,11,3500.00,'Classic','2025-11-20'),

(12,'Lakshmi','9876543221','lakshmi@gmail.com','Female',22,'Salem','2024-11-11',TRUE,9,2800.00,'Veg','2025-10-25'),

(13,'Ramesh','9876543222','ramesh@gmail.com','Male',28,'Trichy','2024-12-01',TRUE,19,7000.00,'Non-Veg','2025-12-05'),

(14,'Keerthi','9876543223','keerthi@gmail.com','Female',20,'Madurai','2025-01-10',TRUE,6,1800.00,'Veg','2025-09-15'),

(15,'Manoj','9876543224','manoj@gmail.com','Male',29,'Chennai','2025-02-14',TRUE,22,8500.00,'Non-Veg','2025-12-08'),

(16,'Nisha','9876543225','nisha@gmail.com','Female',21,'Coimbatore','2025-03-03',TRUE,13,3900.00,'Classic','2025-11-01'),

(17,'Arvind','9876543226','arvind@gmail.com','Male',26,'Madurai','2025-04-17',TRUE,16,5000.00,'Veg','2025-11-28'),

(18,'Pooja','9876543227','pooja@gmail.com','Female',23,'Chennai','2025-05-25',TRUE,17,5400.00,'Classic','2025-12-02'),

(19,'Senthil','9876543228','senthil@gmail.com','Male',30,'Salem','2025-06-30',TRUE,28,11000.00,'Non-Veg','2025-12-19'),

(20,'Kavya','9876543229','kavya@gmail.com','Female',22,'Madurai','2025-07-07',TRUE,5,1500.00,'Veg','2025-09-01');
SELECT * FROM Pizza;
SET AUTOCOMMIT=0;
UPDATE Pizza SET price= 500 WHERE pizza_name = 'Farmhouse';
SET SQL_SAFE_UPDATES = 0 ;
rollback;
UPDATE Pizza SET price= 500 WHERE pizza_name = 'BBQ Chicken';
savepoint s1 ;
UPDATE Pizza SET price= 150 WHERE pizza_name = 'Panner Pizza' ;
savepoint s2 ;
rollback to s2;


