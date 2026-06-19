INSERT INTO inventories
(product_id, quantity_available, quantity_reserved, store_location, last_restocked_at)
VALUES
(1, 100, 10, 'Santiago Centro', NOW()),
(2, 50, 5, 'Maipu', NOW()),
(3, 200, 20, 'Providencia', NOW());


INSERT INTO inventory_movements
(inventory_id, type, quantity, moved_at)
VALUES
(1, 'IN', 100, NOW()),
(1, 'RESERVED', 10, NOW()),
(2, 'OUT', 5, NOW()),
(3, 'IN', 200, NOW());