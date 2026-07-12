CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    care_level VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    product_status VARCHAR(50) NOT NULL,
    watering_frequency VARCHAR(50) NOT NULL,
    size VARCHAR(50) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);