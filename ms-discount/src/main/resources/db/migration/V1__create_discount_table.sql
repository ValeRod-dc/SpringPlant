CREATE TABLE tbl_discount (
    discount_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DOUBLE NOT NULL,

    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,

    max_uses INT,
    current_uses INT DEFAULT 0,
    min_purchase_amount DOUBLE DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    applicable_product_ids VARCHAR(500)
);