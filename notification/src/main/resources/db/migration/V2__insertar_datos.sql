INSERT INTO notifications (user_id, type, channel, message, status, sent_at, created_at) VALUES
(1, 'EMAIL', 'ORDER',    'Tu pedido #1001 ha sido recibido y está siendo procesado.',       'SENT',    '2025-05-10 10:05:00', '2025-05-10 10:00:00'),
(1, 'EMAIL', 'SHIPPING', 'Tu pedido #1001 ha sido enviado. Número de guía: TRK-9821.',      'SENT',    '2025-05-11 08:30:00', '2025-05-11 08:25:00'),
(2, 'SMS',   'PAYMENT',  'Pago de $150.00 confirmado para el pedido #1002.',                'SENT',    '2025-05-12 14:10:00', '2025-05-12 14:05:00'),
(2, 'EMAIL', 'PROMO',    'Tienes un 20% de descuento en tu próxima compra. ¡Úsalo hoy!',   'PENDING', NULL,                  '2025-05-13 09:00:00'),
(3, 'EMAIL', 'REVIEW',   'Cuéntanos tu experiencia con el producto que compraste.',          'FAILED',  NULL,                  '2025-05-14 11:00:00'),
(3, 'SMS',   'ORDER',    'Tu pedido #1003 ha sido confirmado.',                              'SENT',    '2025-05-14 12:00:00', '2025-05-14 11:55:00');
