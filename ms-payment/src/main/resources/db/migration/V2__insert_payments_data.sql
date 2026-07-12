-- 1. Pago completado para usuario 1 (order ficticia 100)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id)
VALUES (100, 1, 150.00, 'CREDIT_CARD', 'COMPLETED', CONCAT('txn_', UUID()));

-- 2. Pago completado para usuario 2 (order ficticia 101)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id)
VALUES (101, 2, 200.00, 'DEBIT_CARD', 'COMPLETED', CONCAT('txn_', UUID()));

-- 3. Pago fallido para usuario 3 (order ficticia 102)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id, error_message)
VALUES (102, 3, 300.00, 'TRANSFER', 'FAILED', CONCAT('txn_', UUID()), 'Transferencia rechazada por el banco');

-- 4. Pago pendiente para usuario 4 (order ficticia 103)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id)
VALUES (103, 4, 75.50, 'STRIPE', 'PENDING', CONCAT('txn_', UUID()));

-- 5. Pago completado para la orden real 1 (cliente 101, pero lo asignamos a user_id=1 para pruebas)
-- La orden 1 existe en order_bd (client_id=101, total=36.00)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id)
VALUES (1, 1, 36.00, 'CREDIT_CARD', 'COMPLETED', CONCAT('txn_', UUID()));

-- 6. Pago fallido para la orden real 2 (cliente 102, pero lo asignamos a user_id=2)
-- La orden 2 existe en order_bd (client_id=102, total=125.50, ya pagada, pero podemos tener un fallido)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id, error_message)
VALUES (2, 2, 125.50, 'DEBIT_CARD', 'FAILED', CONCAT('txn_', UUID()), 'Tarjeta bloqueada');

-- 7. Pago completado para usuario 5 (order ficticia 104)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id)
VALUES (104, 5, 99.99, 'CASH', 'COMPLETED', CONCAT('txn_', UUID()));

-- 8. Pago completado para usuario 6 (order ficticia 105)
INSERT INTO payments (order_id, user_id, amount, method, status, transaction_id)
VALUES (105, 6, 49.50, 'CREDIT_CARD', 'COMPLETED', CONCAT('txn_', UUID()));