INSERT INTO carts (user_id, status, created_at, updated_at)
SELECT 1, 'ACTIVE', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = 1);

INSERT INTO carts (user_id, status, created_at, updated_at)
SELECT 2, 'ACTIVE', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = 2);

INSERT INTO carts (user_id, status, created_at, updated_at)
SELECT 3, 'ACTIVE', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = 3);

INSERT INTO carts (user_id, status, created_at, updated_at)
SELECT 5, 'ACTIVE', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = 5);

INSERT INTO carts (user_id, status, created_at, updated_at)
SELECT 6, 'ACTIVE', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM carts WHERE user_id = 6);

-- Almacenar los IDs de los carritos recién insertados (o existentes)
SET @cart1 = (SELECT cart_id FROM carts WHERE user_id = 1);
SET @cart2 = (SELECT cart_id FROM carts WHERE user_id = 2);
SET @cart3 = (SELECT cart_id FROM carts WHERE user_id = 3);
SET @cart5 = (SELECT cart_id FROM carts WHERE user_id = 5);
SET @cart6 = (SELECT cart_id FROM carts WHERE user_id = 6);

-- Insertar ítems para cada carrito (product_id 1, 2, 3 asumiendo que existen en ms-productos)
-- Evita duplicados por combinación (cart_id, product_id) gracias a la restricción UNIQUE definida en V1.

-- Carrito 1 (usuario 1): producto 1 (cantidad 2, precio 15000) y producto 2 (cantidad 1, precio 25000)
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal, created_at)
SELECT @cart1, 1, 2, 15000.00, 30000.00, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cart1 AND product_id = 1);

INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal, created_at)
SELECT @cart1, 2, 1, 25000.00, 25000.00, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cart1 AND product_id = 2);

-- Carrito 2 (usuario 2): producto 3 (cantidad 3, precio 10000)
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal, created_at)
SELECT @cart2, 3, 3, 10000.00, 30000.00, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cart2 AND product_id = 3);

-- Carrito 3 (usuario 3): producto 1 (cantidad 1, precio 15000)
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal, created_at)
SELECT @cart3, 1, 1, 15000.00, 15000.00, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cart3 AND product_id = 1);

-- Carrito 5 (usuario 5): producto 2 (cantidad 2, precio 25000)
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal, created_at)
SELECT @cart5, 2, 2, 25000.00, 50000.00, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cart5 AND product_id = 2);

-- Carrito 6 (usuario 6): producto 3 (cantidad 1, precio 10000)
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal, created_at)
SELECT @cart6, 3, 1, 10000.00, 10000.00, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE cart_id = @cart6 AND product_id = 3);