CREATE TABLE inventories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity_available INT NOT NULL,
    quantity_reserved INT NOT NULL,
    store_location VARCHAR(255) NOT NULL,
    last_restocked_at DATETIME
);

CREATE TABLE inventory_movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL,
    type ENUM('IN', 'OUT', 'RESERVED') NOT NULL,
    quantity INT NOT NULL,
    moved_at DATETIME,
    CONSTRAINT fk_inventory_movement_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES inventories(id)
        ON DELETE CASCADE
);